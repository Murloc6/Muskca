Last Update: 15 September 2014

Basic commands for testing the provided queries:
java -jar oa4qa.jar cmt ekaw cmt query/qb1.txt data/alignments/$MatcherName-cmt-ekaw.rdf data/reference/cmt-ekaw.rdf
java -jar oa4qa.jar cmt conference cmt query/qb2.txt data/alignments/$MatcherName-cmt-conference.rdf data/reference/cmt-conference.rdf
java -jar oa4qa.jar edas ekaw edas query/qb3.txt data/alignments/$MatcherName-edas-ekaw.rdf data/reference/edas-ekaw.rdf
java -jar oa4qa.jar confof iasted confof query/qb4.txt data/alignments/$MatcherName-confof-iasted.rdf data/reference/confof-iasted.rdf
java -jar oa4qa.jar conference sigkdd conference query/qb5.txt data/alignments/$MatcherName-conference-sigkdd.rdf data/reference/conference-sigkdd.rdf
java -jar oa4qa.jar conference sigkdd conference query/qb6.txt data/alignments/$MatcherName-cmt-edas.rdf data/reference/cmt-edas.rdf
java -jar oa4qa.jar confof ekaw ekaw query/qv1.txt data/alignments/$MatcherName-confof-ekaw.rdf data/reference/confof-ekaw.rdf
java -jar oa4qa.jar iasted sigkdd iasted query/qv2.txt data/alignments/$MatcherName-iasted-sigkdd.rdf data/reference/iasted-sigkdd.rdf
java -jar oa4qa.jar iasted sigkdd iasted query/qv3.txt data/alignments/$MatcherName-iasted-sigkdd.rdf data/reference/iasted-sigkdd.rdf
java -jar oa4qa.jar confof ekaw confof query/qv4.txt data/alignments/$MatcherName-confof-ekaw.rdf data/reference/confof-ekaw.rdf
java -jar oa4qa.jar conference sigkdd conference query/qv5.txt data/alignments/$MatcherName-conference-sigkdd.rdf data/reference/conference-sigkdd.rdf
java -jar oa4qa.jar conference sigkdd conference query/qv6.txt data/alignments/$MatcherName-conference-confof.rdf data/reference/conference-confof.rdf
java -jar oa4qa.jar conference sigkdd conference query/qv7.txt data/alignments/$MatcherName-confof-ekaw.rdf data/reference/confof-ekaw.rdf
java -jar oa4qa.jar confof edas confof query/qa1.txt data/alignments/$MatcherName-confof-edas.rdf data/reference/confof-edas.rdf
java -jar oa4qa.jar cmt confof confof query/qa2.txt data/alignments/$MatcherName-cmt-confof.rdf data/reference/cmt-confof.rdf
java -jar oa4qa.jar cmt ekaw ekaw query/qa3.txt data/alignments/$MatcherName-cmt-ekaw.rdf data/reference/cmt-ekaw.rdf
java -jar oa4qa.jar conference ekaw ekaw query/qa4.txt data/alignments/$MatcherName-conference-ekaw.rdf data/reference/conference-ekaw.rdf
java -jar oa4qa.jar confof edas edas query/qa5.txt data/alignments/$MatcherName-confof-edas.rdf data/reference/confof-edas.rdf

Query:
- The used query engine is an Hermit Extension, available at https://code.google.com/p/owl-bgp/, refer to this page for 
more details on the supported SPARQL fragment and how to write a query.
- For technical reasons the select statement is limited to the variable "?x" only, but the query can use any variable with arbitrary names.
- The materialised results will refer only to variable "?x", and the precision/recall/f-measure will only refer to the values associated with 
this variable.
- The provided set of queries is a subset of the dataset that will compose the OA4QA evaluation; 
they can be considered stable, as well as the syntethic ABoxes for conference ontologies. 
Any modification to the set of queries and to the ontologies will be carried out only for bug fixing purposes.

Ontologies and alignments:
- At the moment the jar file only supports queries againts the ontologies in the conference dataset.
- The performance of an alignment for the suitable query tasks can be evaluated by launching the jar using the 
path of the alignment file, instead of "$MatcherName-onto1-onto2.rdf". In order to be able to run the query 
it is required to respect the name format for the alignment, and to be consistent with the first two parameters 
provided to the jar file.
- The aligned ontologies used during the evaluation (composed by the query ontology in its original form, the 
considered alignment and the other ontology enriched with the syntethic ABox) are materialised in the subfolder 
"aligned"

How to read the results:
- The result set for each submitted query is materialised in a text file in "result" subfolder (both referring to 
computed alignment and reference alignment)
- In the console the Precision/Recall/F-measure are given as output
- Most of the query results can be double checked using DLQuery Tab of Protege against the aligned ontology

Summary of the file and folders provided:
data/aligned: location where aligned ontologies are materialised
data/alignments: oaei 2013 alignments computed by conference track participants
data/filled: oaei 2013 conference ontologies with a syntethic ABox
data/original: oaei 2013 original conference ontologies
data/reference: oaei 2013 ra1 reference alignment for conference track
query: folder with the 15 queries that compose a subset of that of OA4QA
results: where the result sets are materialised
ser: folder used as a temporary storage by the query engine, do not delete it
oa4qa.jar: the jar file providing the query ansering service
readme.txt: this readme file

Acknowledgments:
We thank Ilianna Kollia and Birte Glimm, the authors of the Hermit extension for the tool and their support.

Contacts:
For any comment/information/problem please feel free to contact alessandro [dot] solimando [at] unige [dot] it or 
ernesto [at] cs [.] ox [.] ac [.] uk or ernesto [.] jimenez [.] ruiz [at] gmail [.] com
