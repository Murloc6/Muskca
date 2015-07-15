Muskca - MUlti Sources Knowledge CAndidate 
======

Muskca is a java project to generate candidates from several knowledge bases. This is a prototype for research thesis (http://link.springer.com/chapter/10.1007/978-3-319-13674-5_29). The main idea is to find out if an ontological element or a relation exists in several sources, then a trust score is computed depending of the number sources involved in the candidate. Then an extension is computed to deal with the incompatibles that can appear between candidates. A supervised system is used to discover the optimal extension, the subset of candidates that are not incompatible and maximise the trust.

----------

TODO: 
---------
* Web services architecture (c.f. MuskcaWS projet (not versioned yet))
* Search and remove dead source code
* Clean up the source code (the commented code especially)
* Use the SparqlLib project to generate the sparql queries (https://github.com/NSeydoux/SparqlLib)


> **Dependencies:**

> - An ontological module from which the source knowledge bases have been built (c.f. http://link.springer.com/chapter/10.1007/978-3-319-13674-5_29). The module owl file has to be in the "in" directory.
> - An Fuseki server with all the sources loaded in separate dataset (https://jena.apache.org/documentation/serving_data/)
> - GLPK solver must be installed on your computer (http://glpk-java.sourceforge.net/)

Configuration:
---------------
The file "in/muskca_params.json" contains all the configuration needed.
- "projectName": The name of the fusion project
- "moduleFile": The path to the module file (prefered "in/*")
- "sources": An array which contains all the sources considered: 
-- "name": the source name
-- "baseUri": the base uri used in the source
-- "spIn": the dataset url to querying the Fuseki server
-- "sourceQuality": the source quality consideration (between 0 and 1)
- "relImps": a string array containing the uri of the object properties used to generate relation candidates
- "labelRelImps": a string array containing the uri of the data properties used to generate label candidates
- "uriTypeBase": the uri base for the classes from the module (used to generate the type candidates)
- "uriTypeImps" a string array containing the uri of the classes from which the individual candidates could bo typed of
- "output": an object containing the export options
-- "spOutProvo": the dataset url where to export the result in the fuseki server
-- "provoFile": the path to the provo ontology file (prefered "in/*")
-- "baseUri": the base uri used to generate the new ontological elements in the export format
-- "aligner": the chosen aligner (whose seals version is already in the aligners file) just give the name of the file

Example : 
```
{
  "projectName" : "mini_Triticum",
  "moduleFile" : "in/agronomicTaxon.owl",
  "sources" :
  [
    {
      "name" : "Agrovoc",
      "baseUri" : "http://aims.fao.org/aos/agrovoc/",
      "spIn" : "http://amarger.murloc.fr:8080/Agrovoc_mini_Triticum/",
      "sourceQuality" : 0.6
    },
    {
      "name" : "TaxRef",
      "baseUri" : "http://inpn.mnhn.fr/espece/cd_nom/",
      "spIn" : "http://amarger.murloc.fr:8080/TaxRef_mini_Triticum/",
      "sourceQuality" : 0.9
    },
    {
      "name" : "NCBI",
      "baseUri" : "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=",
      "spIn" : "http://amarger.murloc.fr:8080/NCBI_mini_Triticum/",
      "sourceQuality" : 0.8
    }
  ],
  "relImps" :
  [
    "http://ontology.irstea.fr/AgronomicTaxon#hasHigherRank",
    "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
  ],
  "labelRelImps" :
  [
    "http://ontology.irstea.fr/AgronomicTaxon#hasScientificName",
    "http://ontology.irstea.fr/AgronomicTaxon#hasVernacularName"
  ],
  "uriTypeBase" : "http://ontology.irstea.fr/AgronomicTaxon",
  "uriTypeImps" : 
  [
  	"http://ontology.irstea.fr/AgronomicTaxon#Taxon"
  ],
  "output" :
  {
    "spOutProvo" : "http://amarger.murloc.fr:8080/Muskca-provo/",
    "provoFile" : "in/prov-o.owl",
    "baseUri" : "http://muskca_system.fr/"
  },
  "aligner" : "logmap2"
}

```
