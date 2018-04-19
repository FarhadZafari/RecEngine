/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.conf.Configuration.Resource;
import net.librec.data.DataModel;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.MAEEvaluator;
import net.librec.filter.GenericRecommendedFilter;
import net.librec.filter.RecommendedFilter;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.UserKNNRecommender;
import net.librec.recommender.item.RecommendedItem;
import net.librec.similarity.CosineSimilarity;
import net.librec.similarity.RecommenderSimilarity;

/**
 *
 * @author fzafari
 */
public class KNN {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws LibrecException {
        // TODO code application logic here
        // recommender configuration
	Configuration conf = new Configuration();
	Resource resource = new Resource("/Users/fzafari/Librec/librec/core/src/main/resources/rec/cf/itemknn-test.properties");
	conf.addResource(resource);

	// build data model
	DataModel dataModel = new TextDataModel(conf);
	dataModel.buildDataModel();
        
        conf.set("rec.recommender.similarities","user");
        RecommenderSimilarity similarity = new CosineSimilarity();
        similarity.buildSimilarityMatrix(dataModel);
        RecommenderContext context = new RecommenderContext(conf, dataModel, similarity);

        conf.set("rec.neighbors.knn.number","50");
        conf.set("rec.recommender.isranking","false");

        Recommender recommender = new UserKNNRecommender();
        recommender.recommend(context);

	// evaluation
	RecommenderEvaluator evaluator = new MAEEvaluator();
	recommender.evaluate(evaluator);

	// recommendation results
	List<RecommendedItem> recommendedItemList = recommender.getRecommendedList();
	RecommendedFilter filter = new GenericRecommendedFilter();
	recommendedItemList = filter.filter(recommendedItemList);
    }
    
}
