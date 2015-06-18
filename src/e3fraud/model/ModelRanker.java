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

import com.hp.hpl.jena.rdf.model.Resource;
import static java.lang.Math.max;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Dan
 */
public class ModelRanker {

    /**
     * Simply transforms the Set of models into an ordered List of models,
     * ranked by loss of actor
     *
     * @param actor the actor whose loss to sort by
     * @param models the set of models to sort
     * @return a sorted list from highest to lowest loss for Actor
     */
    public static List<E3Model> sortByLoss(Set<E3Model> models, Resource actor) {
        //make sure all models are enhanced                               
        for (E3Model modelToCompare : models) {
            modelToCompare.enhance();
        }

        List<E3Model> sortedList = new ArrayList<>();
        for (E3Model modelToPlace : models) {

            //if the list is empty
            if (sortedList.isEmpty()) {
                //add it directly
                //System.out.println("ADDED FIRST MODEL TO THE LIST");
                sortedList.add(modelToPlace);
            } else {
                Iterator<E3Model> iterator = sortedList.listIterator();
                //for each model in the sorted list
                while (iterator.hasNext()) {
                    E3Model modelInList = iterator.next();
                    //when we find a bigger one
                    if (modelToPlace.getTotalForActor(actor, false) < modelInList.getTotalForActor(actor, false)) {
                        //add it right before it
                        sortedList.add(sortedList.indexOf(modelInList), modelToPlace);
                        //System.out.println("ADDED ANOTHER MODEL TO THE LIST");    
                        break;
                    }
                }
                //otherwise add it last
                if (!sortedList.contains(modelToPlace)) {
                    sortedList.add(modelToPlace);
                    //System.out.println("ADDED ANOTHER MODEL TO botton of the  LIST");   
                }
            }
        }
        return sortedList;
    }

    /**
     * Transforms the Set of models into an ordered List of models, ranked by
     * average loss of given actor across a specified interval of occurrence of
     * a given need.
     *
     * @param actor the actor whose loss to sort by
     * @param models the set of models to sort
     * @param need the need whose occurrence varies
     * @param startValue the minimum occurrence rate of the need
     * @param endValue the maximum occurrence rate of the need
     * @param ideal whether or not we should calculate this for the ideal case
     * or a sub-ideal case
     * @return a sorted list from highest to lowest loss for Actor
     */
    public static List<E3Model> sortByLoss(Set<E3Model> models, Resource actor, Resource need, int startValue, int endValue, boolean ideal) {
        //make sure all models are enhanced                               
        for (E3Model modelToCompare : models) {
            modelToCompare.enhance();
        }

        List<E3Model> sortedList = new ArrayList<>();
        for (E3Model modelToPlace : models) {
            float modelToPlaceAverage = modelToPlace.getAverageForActor(actor, need, startValue, endValue, false);
            //if the list is empty
            if (sortedList.isEmpty()) {
                //add it directly
                //System.out.println("ADDED FIRST MODEL TO THE LIST");
                sortedList.add(modelToPlace);
            } else {
                Iterator<E3Model> iterator = sortedList.listIterator();
                while (iterator.hasNext()) {
                    E3Model modelInList = iterator.next();
                    float modelInListAverage = modelInList.getLastKnownAverage();
                    //when we find a bigger one
                    if (modelToPlaceAverage < modelInListAverage) {
                        //add it right before it
                        sortedList.add(sortedList.indexOf(modelInList), modelToPlace);
                        //System.out.println("ADDED ANOTHER MODEL TO THE LIST");    
                        break;
                    }
                }
                //otherwise add it last
                if (!sortedList.contains(modelToPlace)) {
                    sortedList.add(modelToPlace);
                    //System.out.println("ADDED ANOTHER MODEL TO botton of the  LIST");   
                }
            }
        }
        return sortedList;
    }

    /**
     * Transforms the Set of models into an ordered List of models, ranked by
     * average loss of given actor across a specified interval of occurrence of
     * a given need. In case two models are equal in terms of loss, they are
     * ranked by largest gain of any other actor compared to baseModel
     *
     * @param baseModel baseModel to compare gain to
     * @param actor the actor whose loss to sort by
     * @param models the set of models to sort
     * @param need the need whose occurrence varies
     * @param startValue the minimum occurrence rate of the need
     * @param endValue the maximum occurrence rate of the need
     * @param ideal whether or not we should calculate this for the ideal case
     * or a sub-ideal case
     * @return a sorted list from highest to lowest loss for Actor
     */
    public static List<E3Model> sortByLossandGain(E3Model baseModel, Set<E3Model> models, Resource actor, Resource need, int startValue, int endValue, boolean ideal) {
        boolean found = false;

        //make sure all models are enhanced                               
        for (E3Model modelToCompare : models) {
            modelToCompare.enhance();
        }
        baseModel.enhance();
        Map<Resource, Float> baseModelAverages = baseModel.getAveragesForActors(need, startValue, endValue, true);

        List<E3Model> sortedList = new ArrayList<>();
        //for each model
        for (E3Model modelToPlace : models) {
            Map<Resource, Float> modelToPlaceAverages = modelToPlace.getAveragesForActors(need, startValue, endValue, ideal);
            //if the list is empty
            if (sortedList.isEmpty()) {
                //add it directly
                //System.out.println("ADDED FIRST MODEL TO THE LIST");
                sortedList.add(modelToPlace);
                //otherwise
            } else {
                Iterator<E3Model> iterator = sortedList.listIterator();
                //for each model already in the sorted list
                while (iterator.hasNext()) {
                    //check it's average
                    E3Model modelInList = iterator.next();
                    Map<Resource, Float> modelInListAverages = modelInList.getLastKnownAverages();
                    //when we find a bigger one  (for selected actor)
                    //System.out.println("Comparing "+ modelToPlaceAverages.get(actor)+ " with "+ modeInListAverages.get(actor));
                    if (modelToPlaceAverages.get(actor) < modelInListAverages.get(actor)) {
                        //add it right before it
                        sortedList.add(sortedList.indexOf(modelInList), modelToPlace);
                        //System.out.println("Found bigger loss. ADDED ANOTHER MODEL TO THE LIST");    
                        break;
                    } //if it happens to be equal, we need to sort by delta of gain of other actors
                    else if (modelToPlaceAverages.get(actor).equals(modelInListAverages.get(actor))) {
                        found = false;
                        //check if any actor has higher gain DELTA (!)
                        float gainDeltaToPlace = -999999999;
                        float gainDeltaInList = -999999999;
                        for (Resource actorInModelToPlace : modelToPlace.getActors()) {
                            if (actorInModelToPlace.getURI().equals(modelToPlace.newActorURI)) {
                                Resource colludedActor = baseModel.getJenaModel().getResource(modelToPlace.colludedActorURI);
                                gainDeltaToPlace = max(gainDeltaToPlace, modelToPlaceAverages.get(actorInModelToPlace) - baseModelAverages.get(actorInModelToPlace) - baseModelAverages.get(colludedActor));
                            } else {
                                gainDeltaToPlace = max(gainDeltaToPlace, modelToPlaceAverages.get(actorInModelToPlace) - baseModelAverages.get(actorInModelToPlace));
                            }
                        }
                        for (Resource actorInModelInList : modelInList.getActors()) {
                            if (actorInModelInList.getURI().equals(modelToPlace.newActorURI)) {
                                Resource colludedActor = baseModel.getJenaModel().getResource(modelToPlace.colludedActorURI);
                                gainDeltaInList = max(gainDeltaInList, modelInListAverages.get(actorInModelInList) - baseModelAverages.get(actorInModelInList) - baseModelAverages.get(colludedActor));
                            } else {
                                gainDeltaInList = max(gainDeltaInList, modelInListAverages.get(actorInModelInList) - baseModelAverages.get(actorInModelInList));
                            }
                        }
                        if (gainDeltaToPlace > gainDeltaInList) {
                                    //and add it right before it
                            //System.out.println("Found lower gain. ADDED ANOTHER MODEL TO THE LIST");
                            sortedList.add(sortedList.indexOf(modelInList), modelToPlace);
                            break;
                        }
                    }
                }
                //otherwise add it last
                if (!sortedList.contains(modelToPlace)) {
                    sortedList.add(modelToPlace);
                    //System.out.println("ADDED ANOTHER MODEL TO botton of the  LIST");   

                }
            }

        }
        return sortedList;
    }

}
