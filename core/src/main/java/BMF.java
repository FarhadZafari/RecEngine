
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import me.tongfei.progressbar.ProgressBar;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.conf.Configuration.Resource;
import net.librec.data.DataModel;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.RMSEEvaluator;
import net.librec.filter.GenericRecommendedFilter;
import net.librec.filter.RecommendedFilter;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.rating.PMFRecommender;
import net.librec.recommender.item.RecommendedItem;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author fzafari
 */
public class BMF {

    public static void main(String[] args) throws LibrecException, IOException, InterruptedException {

        // recommender configuration
        Configuration conf = new Configuration();
        Resource resource = new Resource("rec/cf/rating/pmf-test.properties");
        conf.addResource(resource);

        System.out.println("The setting of PMF method is loaded with: "
                + conf.get("rec.iterator.maximum") + " iterations and "
                + conf.get("rec.factor.number") + " factors...");

        // build data model
        DataModel dataModel = new TextDataModel(conf);

        //Splitting data is done from inside this function:
        dataModel.buildDataModel();

        RecommenderContext context = new RecommenderContext(conf, dataModel);
        PMFRecommender recommender = new PMFRecommender();
        recommender.recommend(context);
        System.out.println("Total number of users is: " + recommender.userMappingData.size() + " total number of items is: " + recommender.itemMappingData.size());

        // evaluation
        RecommenderEvaluator RMSE = new RMSEEvaluator();
        System.out.println("RMSE on the test set is: " + recommender.evaluate(RMSE));

        //Recommend for a single user:
        long start = System.currentTimeMillis();
        recommender.recommendRank(0, 5);
        System.out.println("Recommendation for one single user is generated in " + (System.currentTimeMillis() - start) + " milliseconds!");

        recommender.isRanking = true;
        recommender.topN = 5;
        recommender.recommendRank();

        //RecommenderEvaluator AUC = new AUCEvaluator();
        //System.out.println("AUC: " + recommender.evaluate(AUC));

        // recommendation results
        List<RecommendedItem> recommendedItemList = recommender.getRecommendedList();
        RecommendedFilter filter = new GenericRecommendedFilter();
        recommendedItemList = filter.filter(recommendedItemList);

        ProgressBar pb = new ProgressBar("Write recommendations", recommendedItemList.size());
        pb.start();
        BufferedWriter writer = new BufferedWriter(new FileWriter("Recommendations.txt"));
        for (int i = 0; i < recommendedItemList.size(); i++) {
            //System.out.println(recommendedItemList.get(i).getUserId() + " " + recommendedItemList.get(i).getItemId());
            writer.write(recommendedItemList.get(i).getUserId() + " " + recommendedItemList.get(i).getItemId() + " " + recommendedItemList.get(i).getValue() + "\n");
            pb.step();
        }
        writer.flush();
        pb.stop();


        String command = "sh /Users/fzafari/librec-latest/core/04_generate_recs";
        Process proc = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line = "";
        pb = new ProgressBar("Gnerate recommendation pickle file", recommendedItemList.size());
        pb.start();
        while ((line = reader.readLine()) != null) {
            System.out.print(line + "\n");
            pb.step();
        }
        proc.waitFor();
        pb.stop();

        command = "sh /Users/fzafari/librec-latest/core/05_eval_algo_svd";
        proc = Runtime.getRuntime().exec(command);
        reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        line = "";
        pb = new ProgressBar("Gnerate evaluation file", recommendedItemList.size());
        pb.start();
        while ((line = reader.readLine()) != null) {
            System.out.print(line + "\n");
            pb.step();
        }
        proc.waitFor();
        pb.stop();
    }
}
