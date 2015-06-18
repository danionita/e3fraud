/*
 * Copyright (C) 2015 Dan Ionita 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package e3fraud.model;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import e3fraud.vocabulary.E3value;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

/**
 *
 * @author Dan
 */
public class SubIdealModelGenerator {

    public Set<E3Model> generateAll(E3Model baseModel, Resource mainActor) {

        System.out.println("GENERATING MODELS...\n\n\n");
        Set<E3Model> subIdealModels = new HashSet<>();
        Set<E3Model> colludedModels = generateCollusions(baseModel, mainActor);
        Set<E3Model> hiddenModels = generateHiddenTransactions(baseModel, mainActor);
        Set<E3Model> nonOccuringModels = generateNonoccurringTransactions(baseModel);
        Set<E3Model> colludedAndNonOccuringModels = new HashSet<>();
        Set<E3Model> hiddenAndNonOccuringModels = new HashSet<>();
        Set<E3Model> colludedHiddenAndNonOccuringModels = new HashSet<>();

        //for each combination of collusion
        for (E3Model colludedModel : colludedModels) {
            //generate all possible combinations of non-occuring transactions to the result
            colludedAndNonOccuringModels.addAll(generateNonoccurringTransactions(colludedModel));
        }

        //for each combination of nonOccuraning transactions
        for (E3Model nonOccuringModel : nonOccuringModels) {
            //generate all possible combinations of hidden transactions to the result
            hiddenAndNonOccuringModels.addAll(generateHiddenTransactions(nonOccuringModel, mainActor));
        }

        //for each combination of collusion and non-occuring transaction
        for (E3Model colludedAndNonOccuringModel : colludedAndNonOccuringModels) {
            //generate all possible combinations of hidden transactions
            colludedHiddenAndNonOccuringModels.addAll(generateHiddenTransactions(colludedAndNonOccuringModel, mainActor));
        }

        //*********TEST STUFF***************
//        System.out.println("\nGENERATING colludedModels");
//        for (E3Model generatedModel : colludedModels) {
//            System.out.println("Generated:" + generatedModel.getDescription());
//        }
//        System.out.println("\nGENERATING hiddenModels");
//        for (E3Model generatedModel : hiddenModels) {
//            System.out.println("generated:" + generatedModel.getDescription());
//        }
//        System.out.println("\nGENERATING nonOccuringModels");
//        for (E3Model generatedModel : nonOccuringModels) {
//            System.out.println("Generated:" + generatedModel.getDescription());
//        }
//        System.out.println("\nGENERATING hiddenAndNonOccuringModels");
//        for (E3Model generatedModel : hiddenAndNonOccuringModels) {
//            System.out.println("Generated:" + generatedModel.getDescription());
//        }
//        System.out.println("\nGENERATING colludedAndNonOccuringModels");
//        for (E3Model generatedModel : colludedAndNonOccuringModels) {
//            System.out.println("Generated:" + generatedModel.getDescription());
//        }
//        System.out.println("\nGENERATING colludedHiddenAndNonOccuringModels");
//        for (E3Model generatedModel : colludedHiddenAndNonOccuringModels) {
//            System.out.println("Generated:" + generatedModel.getDescription());
//        }
        //*******END OF TEST STUFF*************
        subIdealModels.addAll(colludedModels);
        subIdealModels.addAll(hiddenModels);
        subIdealModels.addAll(nonOccuringModels);
        subIdealModels.addAll(hiddenAndNonOccuringModels);
        subIdealModels.addAll(colludedAndNonOccuringModels);
        subIdealModels.addAll(colludedHiddenAndNonOccuringModels);

        return subIdealModels;
    }

    public Set<E3Model> generateCollusions(E3Model baseModel, Resource mainActor) {
        Set<E3Model> subIdealModels = new HashSet<E3Model>();
        Set<Resource> secondaryActors = baseModel.getActors();
        secondaryActors.remove(mainActor);

        //generate combinations of 2 secondary actors
        ICombinatoricsVector<Resource> secondayActorsVector = Factory.createVector(secondaryActors);
        Generator<Resource> secondaryActorsCombinations = Factory.createSimpleCombinationGenerator(secondayActorsVector, 2);

        //for each combination of two secondary actors:
        for (ICombinatoricsVector<Resource> secondaryActorsCombination : secondaryActorsCombinations) {

            //Create an empty model
            Model model = ModelFactory.createDefaultModel();
            model.add(baseModel.getJenaModel());
            E3Model generatedModel = new E3Model(model, baseModel.getDescription());

            //just in case previous method did not change anything(if this method was called directly, instead of calling generateAll)
            if (generatedModel.getDescription().equals("Base Model")) {
                generatedModel.setDescription("");
            }

            Resource actor1 = secondaryActorsCombination.getValue(0);
            Resource actor2 = secondaryActorsCombination.getValue(1);

            generatedModel.collude(actor1, actor2);
            generatedModel.setDescription(generatedModel.getDescription() + "Actors  \"" + actor1.getProperty(E3value.e3_has_name).getLiteral().toString() + "\" and \"" + actor2.getProperty(E3value.e3_has_name).getLiteral().toString() + "\" are colluding.\n");
            subIdealModels.add(generatedModel);
        }
        return subIdealModels;
    }

    /**
     *
     * @param baseModel
     * @return a set of models derived from baseModel with all possible
     * combinations of non-occurring (dotted) transactions
     */
    public Set<E3Model> generateNonoccurringTransactions(E3Model baseModel) {
        Set<E3Model> subIdealModels = new HashSet<E3Model>();
        Set<Resource> moneyExchanges = baseModel.getMoneyExchanges();

        // Create the initial vector
        ICombinatoricsVector<Resource> initialMoneyExchangesVector = Factory.createVector(moneyExchanges);
        for (int i = 1; i <= moneyExchanges.size(); i++) {
            Generator<Resource> moneyExchangeCombinations = Factory.createSimpleCombinationGenerator(initialMoneyExchangesVector, i);

            for (ICombinatoricsVector<Resource> moneyExchangeCombination : moneyExchangeCombinations) {

                //Create an empty model
                Model model = ModelFactory.createDefaultModel();
                model.add(baseModel.getJenaModel());
                E3Model generatedModel = new E3Model(model, baseModel.getDescription());

                //just in case previous method did not change anything(if this method was called directly, instead of calling generateAll)
                if (generatedModel.getDescription().equals("Base Model")) {
                    generatedModel.setDescription("");
                }

                //iterate through the elements of the combination
                Iterator<Resource> moneyExchangeIterator = moneyExchangeCombination.iterator();
                while (moneyExchangeIterator.hasNext()) {
                    //and update new model accordingly
                    Resource exchange = moneyExchangeIterator.next();
                    generatedModel.makeDotted(exchange);
                    generatedModel.setDescription(generatedModel.getDescription() + "Exchange " + exchange.getProperty(E3value.e3_has_name).getLiteral().toString() + " does not take place.\n");
                }
                //System.out.println("Generated:" + generatedModel.getDescription());
                subIdealModels.add(generatedModel);
            }
        }

        return subIdealModels;
    }

    /**
     *
     * @param baseModel
     * @param mainActor
     * @return a set of models derived from baseModel with all possible
     * combinations of hidden (dashed) transactions
     */
    public Set<E3Model> generateHiddenTransactions(E3Model baseModel, Resource mainActor) {
        Set<E3Model> subIdealModels = new HashSet<E3Model>();
        Set<Resource> secondaryActors = baseModel.getActors();
        secondaryActors.remove(mainActor);
        baseModel.enhance();

        //generate combinations of 2 secondary actors
        ICombinatoricsVector<Resource> secondayActorsVector = Factory.createVector(secondaryActors);
        Generator<Resource> secondaryActorsCombinations = Factory.createSimpleCombinationGenerator(secondayActorsVector, 2);

        //for each combination of two secondary actors:
        for (ICombinatoricsVector<Resource> secondaryActorsCombination : secondaryActorsCombinations) {
            Resource actor1 = secondaryActorsCombination.getValue(0);
            Resource actor2 = secondaryActorsCombination.getValue(1);

            //check if they  have a transaction between them
            Map<Resource, Resource> commonInterfaces = baseModel.getInterfacesBetween(actor1, actor2);

            //and if they do
            if (!commonInterfaces.isEmpty()) {
                
                Iterator commonInterfaceIterator = commonInterfaces.entrySet().iterator();
                //for each pair of (unique) common interfaces
                while (commonInterfaceIterator.hasNext()) {
                    Map.Entry<Resource, Resource> commonInterfacePair = (Map.Entry) commonInterfaceIterator.next();
                    //We need to get the total of each corresponding actor FOR THE CORRESPONDING DEPENDENCY PATH, in the ideal case
                    Resource interface1 = commonInterfacePair.getKey();
                    float actor1Total = baseModel.getTotalForActorPerOccurence(actor1, interface1, true);
                    Resource interface2 = commonInterfacePair.getValue();
                    float actor2Total = baseModel.getTotalForActorPerOccurence(actor2, interface2, true);
                    //and create outgoing hidden transactions for each actor of up to the previously calculated total:

                    //Generate models with money flows in each direction , ranging from 0 to the total Profit of the actor;            

                    //if actor1 has a positive financial result
                    
                    if (actor1Total > 0) {
                        //divide this result into 10 values
                        float step = actor1Total / 10;

                        //and for each value
                        for (float value = 0; value <= actor1Total; value = value + step) {

                            //Create an empty model
                            Model model = ModelFactory.createDefaultModel();
                            model.add(baseModel.getJenaModel());
                            E3Model generatedModel = new E3Model(model, baseModel.getDescription());

                            //just in case previous method did not change anything (if this method was called directly, instead of calling generateAll)
                            if (generatedModel.getDescription().equals("Base Model")) {
                                generatedModel.setDescription("");
                            }

                            //add a transfer from actor1 to actor 2 of the value
                            generatedModel.addTransfer(interface1, interface2, value);
                            generatedModel.setDescription(generatedModel.getDescription() + "Hidden transfer of value " + value + " (out of "+ actor1Total+") from \"" + actor1.getProperty(E3value.e3_has_name).getLiteral().toString() + "\" to \"" + actor2.getProperty(E3value.e3_has_name).getLiteral().toString() + "\"\n");
                            subIdealModels.add(generatedModel);
                        }
                    }

                    //if actor2 has a positive financial result
                    if (actor2Total > 0) {
                        //divide this result into 10 values
                        float step = actor2Total / 10;

                        //and for each value
                        for (float value = 0; value <= actor2Total; value = value + step) {

                            //Create an empty model
                            Model model = ModelFactory.createDefaultModel();
                            model.add(baseModel.getJenaModel());
                            E3Model generatedModel = new E3Model(model, baseModel.getDescription());

                            //just in case previous method did not change anything (if this method was called directly, instead of calling generateAll)
                            if (generatedModel.getDescription().equals("Base Model")) {
                                generatedModel.setDescription("");
                            }

                            //add a transfer from actor1 to actor 2 of the value
                            generatedModel.addTransfer(interface2, interface1, value);
                            generatedModel.setDescription(generatedModel.getDescription() + "Hidden transfer of value "  + value + " (out of "+ actor1Total+") from \"" + actor2.getProperty(E3value.e3_has_name).getLiteral().toString() + "\" to \"" + actor1.getProperty(E3value.e3_has_name).getLiteral().toString() + "\n");
                            subIdealModels.add(generatedModel);
                        }
                    }
                }
            }

        }
        return subIdealModels;
    }
}
