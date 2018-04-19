/**
 * Copyright (C) 2016 LibRec
 * <p>
 * This file is part of LibRec. LibRec is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * LibRec is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * LibRec. If not, see <http://www.gnu.org/licenses/>.
 */
package net.librec.recommender.cf.rating;

import java.util.List;
import java.util.Random;
import net.librec.common.LibrecException;
import net.librec.math.structure.MatrixEntry;
import net.librec.recommender.MatrixFactorizationRecommender;

/**
 * <ul>
 * <li><strong>PMF:</strong> Ruslan Salakhutdinov and Andriy Mnih, Probabilistic
 * Matrix Factorization, NIPS 2008.</li>
 * <li><strong>RegSVD:</strong> Arkadiusz Paterek, <strong>Improving Regularized
 * Singular Value Decomposition</strong>
 * Collaborative Filtering, Proceedings of KDD Cup and Workshop, 2007.</li>
 * </ul>
 *
 * @author guoguibin and zhanghaidong
 */
public class PMFRecommender extends MatrixFactorizationRecommender {

    @Override
    protected void setup() throws LibrecException {
        super.setup();
    }

    @Override
    protected void trainModel() throws LibrecException {
        for (int iter = 1; iter <= numIterations; iter++) {

            loss = 0.0d;
            total_error = 0.0d;
            for (MatrixEntry me : trainMatrix) {
                int userId = me.row(); // user
                int itemId = me.column(); // item

                //*****************************************************************************
                //Using these new regularizers yeilds better results than the old ones.
                double reg_users = (double) 1 / Math.sqrt(this.trainMatrix.rowSize(userId));
                double reg_items = (double) 1 / Math.sqrt(this.trainMatrix.columnSize(itemId));
                //*****************************************************************************

                double realRating = me.get();
                double predictRating = predict(userId, itemId);
                double error = realRating - predictRating;

                loss += error * error;
                total_error += error * error;

                // update factors
                for (int factorId = 0; factorId < numFactors; factorId++) {
                    double userFactor = userFactors.get(userId, factorId), itemFactor = itemFactors.get(itemId, factorId);

                    userFactors.add(userId, factorId, learnRate * (error * itemFactor - regUser * reg_users * userFactor));
                    itemFactors.add(itemId, factorId, learnRate * (error * userFactor - regItem * reg_items * itemFactor));

                    loss += regUser * userFactor * userFactor + regItem * itemFactor * itemFactor;
                }

                //Negative sampling:
                //*****************************************************************************
                List<Integer> user_items = trainMatrix.getColumns(userId);
                Random r = new Random();
                do {
                    itemId = r.nextInt(trainMatrix.numColumns);
                } while (user_items.contains(itemId));
                predictRating = predict(userId, itemId);
                error = 0 - predictRating;

                loss += error * error;
                total_error += error * error;

                // update factors
                for (int factorId = 0; factorId < numFactors; factorId++) {
                    double userFactor = userFactors.get(userId, factorId), itemFactor = itemFactors.get(itemId, factorId);

                    userFactors.add(userId, factorId, learnRate * (error * itemFactor - regUser * reg_users * userFactor));
                    itemFactors.add(itemId, factorId, learnRate * (error * userFactor - regItem * reg_items * itemFactor));

                    loss += regUser * userFactor * userFactor + regItem * itemFactor * itemFactor;
                }
                //*****************************************************************************
            }
            total_error = Math.sqrt(total_error / trainMatrix.getData().length / 2);

            loss *= 0.5;
            if (isConverged(iter) && earlyStop) {
                break;
            }
            updateLRate(iter);
        }
    }

    //I added this here myself, because I also want to monitor how the learning rate is decaying!
    @Override
    protected boolean isConverged(int iter) throws LibrecException {
        float delta_loss = (float) (lastLoss - loss);

        // print out debug info
        if (verbose) {
            String recName = getClass().getSimpleName().toString();
            String info = recName + " iter " + iter + ", learning rate: " + learnRate + ", loss = " + loss + ", RMSE on train set = " + total_error + ", delta_loss = " + delta_loss;
            LOG.info(info);
        }

        if (Double.isNaN(loss) || Double.isInfinite(loss)) {
//            LOG.error("Loss = NaN or Infinity: current settings does not fit the recommender! Change the settings and try again!");
            throw new LibrecException("Loss = NaN or Infinity: current settings does not fit the recommender! Change the settings and try again!");
        }

        // check if converged
        boolean converged = Math.abs(delta_loss) < 1e-5;

        return converged;
    }
}
