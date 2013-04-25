Deze plugin is nodig voor de iPOJO manipulatie die in FelinxPlugin wordt uitgevoerd in de builder.
De inhoud van deze jar is de OSGI-ficatie van delen van
* org.apache.felix.ipojo.ant-1.8.6.jar
* org.apache.felix.ipojo.manipulator-1.8.6.jar

Om deze FelinxIPojoManipulator plugin opnieuw te bouwen ga je als volgt te werk:

1) Download de laatste versies van
* org.apache.felix.ipojo.ant-1.8.6.jar
* org.apache.felix.ipojo.manipulator-1.8.6.jar
2) Pak beide archieven uit.
3) Kopier uit folder org.apache.felix.ipojo.ant-1.8.6 de subfolder metadata (die je hier vindt:   org\apache\felix\ipojo\metadata)
   en plak die in de folder org.apache.felix.ipojo.manipulator-1.8.6\org\apache\felix\ipojo\
4) Maak een zip file van de aangepaste folder org.apache.felix.ipojo.manipulator-1.8.6 en geef die de extentie .jar.
5) Maak een Eclipse Plugin project op basis van een bestaande jar en kies deze jar.
6) Voeg opnieuw deze tekst file toe.
7) Exporteer naar de Eclipse plugin folder.