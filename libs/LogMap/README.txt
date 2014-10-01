Copyright 2013 by the Department of Computer Science (University of Oxford)

LogMap is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or(at your option) any later version.

LogMap is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along with LogMap.  If not, see <http://www.gnu.org/licenses/>.



LogMap is a highly scalable ontology matching system with ‘built-in’ reasoning and inconsistency repair capabilities. To the best of our knowledge, LogMap is one of the few matching systems that (1) can efficiently match semantically rich ontologies containing tens (and even hundreds) of thousands of classes, (2) incorporates sophisticated reasoning and repair techniques to minimise the number of logical inconsistencies, and (3) provides support for user intervention during the matching process (this functionality is not provided in this standalone distribution of LogMap).

LogMap can operate as an ontology matching systems (MATCHER) or as a mapping debugging system (DEBUGGER). Additionally it also converts mappings from RDF-OAEI format to OWL.



REQUIREMENTS:

Java 1.6 or higher.

LogMap uses the HermiT 1.3.8, OWL API 3.4.3 (bundled within HermiT), MORe 0.1.6 and ELK 0.4.1 reasoners which are under the LGPL or Apache licenses (see lib folder).



USAGE FROM APPLICATIONS:

- See http://code.google.com/p/logmap-matcher/wiki/LogMapFromApplications
- See http://code.google.com/p/logmap-matcher/wiki/LogMapRepairFromApplications



USAGE FROM COMMAND LINE:

LogMap 2 MATCHER facility requires 5 parameters:
	1. MATCHER. To use the matching functionality.
	2. IRI ontology 1. e.g.: http://myonto1.owl  or  file:/C://myonto1.owl  or  file:/usr/local/myonto1.owl
	3. IRI ontology 2. e.g.: http://myonto2.owl  or  file:/C://myonto2.owl  or  file:/usr/local/myonto2.owl
	4. Full output path for mapping files and overlapping modules/fragments. e.g. /usr/local/output_path/ or C://output_path/
	5. Classify the input ontologies together with the mappings. e.g. true or false

	For example: java -jar logmap2_standalone.jar MATCHER file:/home/ontos/cmt.owl file:/home/ontos/ekaw.owl /home/mappings/output true

Optionally, LogMap matching process can be customized by modifying the "parameters.txt" file. This file contains "thresholds" to include/discard mappings, a flag to approximate the overlapping between the input ontologies, a set of flags to specify if class/property mappings are given as output and if instance mappings are extracted. Finally it also provides the possibility to add additionally annotation URIs to extract the ontology lexicon.



LogMap 2 DEBUGGER facility requires 8 parameters:
	1. DEBUGGER. To use the debugging facility.
	2. IRI ontology 1. e.g.: http://myonto1.owl  or  file:/C://myonto1.owl  or  file:/usr/local/myonto1.owl
	3. IRI ontology 2. e.g.: http://myonto2.owl  or  file:/C://myonto2.owl  or  file:/usr/local/myonto2.owl
	4. Format mappings e.g.: OWL  or  RDF  or  TXT
	5. Full IRI or full Path:
		a. Full IRI of input mappings if OWL format. e.g.: file:/C://mymappings.owl  or  file:/usr/local/mymappings.owl  or http://mymappings.owl
		b. or Full path of input mappings if formats RDF or TXT. e.g.: C://mymappings.rdf  or  /usr/local/mymappings.txt
		c. Note that TXT and OWL mappings have an special format. RDF is the same as in the OAEI campaign.
	6. Full output path for the repaired mappings: e.g. /usr/local/output_path or C://output_path
	7. Extract modules (i.e. intersection or overlapping of the ontologies) before repair?: true or false
	8. Check satisfiability after repair using HermiT? true or false

	For example: java -jar logmap2_standalone.jar DEBUGGER file:/home/ontos/cmt.owl file:/home/ontos/ekaw.owl RDF /usr/local/mymappings.rdf /home/mappings/output false true


The RDF2OWL converter facility requires 4 parameters:
	1. RDF2OWL. To transform from RDF-OAEI format to OWL. Note that the input ontologies are required to check the type of entity of the mapped IRIs.
	2. IRI ontology 1. e.g.: http://myonto1.owl  or  file:/C://myonto1.owl  or  file:/usr/local/myonto1.owl
	3. IRI ontology 2. e.g.: http://myonto2.owl  or  file:/C://myonto2.owl  or  file:/usr/local/myonto2.owl
	4. Full path RDF mappings to be converted: e.g. C://mymappings.rdf  or  /usr/local/mymappings.rdf

	For example: java -jar logmap2_standalone.jar RDF2OWL file:/home/ontos/cmt.owl file:/home/ontos/ekaw.owl /usr/local/mymappings.rdf



For large ontologies you may need the following JVM arguments:

	java -Xms500M -Xmx4400M -DentityExpansionLimit=100000000 -jar logmap2_standalone.jar ...



RELEASE NOTES:

LogMap 2.4
------------
 * Uses OWL API 3.4.3 and HermiT 1.3.8
 * MORe reasoner 0.1.6, together with HermiT, has been integrated as candidate OWL 2 reasoner. Can be selected using the parameters file.
 * HermiT is now run ignoring non supported (OWL 2) datatypes.
 * Property matching algorithm exploits now inverses
 * Fixed issue with semantic index related to given negative preorder numbers.
 * Enhanced module extractor.
 * Class type for instances are now extracted using the reasoner.
 * Included "subtypes" case in InstanceAssesment class.
 * Considered stemming to discover weak instance mappings.
 * Instance mappings, apart from the compatibility score, also contains information about their scope (i.e. lexical similarity of their respective class types).
 * Update on the interactivity codes. Adaptation to participate in OAEI 2013.
 * Update on the instance matching module to cope with OAEI 2013 test case.
 * The Web interface has also been enhanced, mainly the issues related to ontologies outside the OWL 2 profile.
 * To avoid the use of non-real emails, the link to the results are only sent via email.



