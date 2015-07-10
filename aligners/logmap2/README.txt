Copyright 2012 by the Department of Computer Science (University of Oxford)

LogMap is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or(at your option) any later version.

LogMap is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along with LogMap.  If not, see <http://www.gnu.org/licenses/>.



LogMap is a highly scalable ontology matching system with ‘built-in’ reasoning and inconsistency repair capabilities. To the best of our knowledge, LogMap  (1) can efficiently match semantically rich ontologies containing tens (and even hundreds) of thousands of classes, (2) incorporates sophisticated reasoning and repair techniques to minimise the number of logical inconsistencies, and (3) provides support for user intervention during the matching process.

LogMap integrates the OWL API and HermiT reasoner which are also under the LGPL license.


REQUIREMENTS:

Java 1.6 or higher.


USAGE:

This distribution of LogMap has been wrapped according the SEALS platform (http://www.seals-project.eu/) requirements in order to participate in the OAEI evaluation campaign (http://oaei.ontologymatching.org/). Section 5.2 of the OAEI wrapping tutorial (http://oaei.ontologymatching.org/2011.5/tutorial/tutorialv3.pdf) explains how to run a SEALS-like tool using the SEALS OMT Client (http://web.informatik.uni-mannheim.de/oaei-interactive/2013/seals-omt-client-v4i.jar).

LogMap matching process can be customized by modifying the "conf/parameters.txt" file. This file contains "thresholds" to include/discard mappings, a flag to approximate the overlapping between the input ontologies, a set of flags to specify if class/property mappings are given as output and if instance mappings are extracted. Finally it also provides the possibility to add additionally annotation URIs to extract the ontology lexicon.


