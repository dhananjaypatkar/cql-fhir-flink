# cql-fhir-flink
CQL evaluator configured as a Apache Flink task to continuously evaluate incoming FHIR patient resources in an input directory  

Change directory paths appropriately in Application.java

Steps
1. Copy relevant valueset files in "valuesets" directory
2. Copy measure cql definitions in "cql" directory
3. Start the application by running Application.java
4. Place patient FHIR json formatted files in "input" directory

TODO: Make measurement period a variable to the application
