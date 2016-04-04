/*
 * Copyright (C) 2015 Dan
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
package test;

import com.hp.hpl.jena.rdf.model.Resource;
import e3fraud.model.E3Model;
import e3fraud.parser.FileParser;
import e3fraud.vocabulary.E3value;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author Dan
 */
public class test {
        public static void main(String[] args) {
        File file = new File("D://temp/test.rdf");
        E3Model baseModel = FileParser.parseFile(file);
        Map<Resource, XYSeries> actorSeriesMap = new HashMap();
        Map<E3Model, Map<Resource, XYSeries>> generatedModels = new HashMap();
        Map<String, Resource> needsMap = baseModel.getNeedsMap();
        String selectedNeedString = "Make call";
        String selectedActorString = "Provider A";
        Set<Resource> actors = baseModel.getActors();
        Map<String, Resource> actorsMap = baseModel.getActorsMap();


        baseModel.enhance();
        for(Resource actor: baseModel.getActors()){
        System.out.println("Total for actor "+ actor.getProperty(E3value.e3_has_name).getLiteral().toString()+ " = "+ baseModel.getTotalForActor(actor, true));
        }

    }


}
