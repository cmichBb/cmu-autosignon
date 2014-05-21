# CMU Customizations to Autosignon B2
As of the April 2014 Release of Blackboard, we are using a modified version of the v1.2 code from the autosignon project from Oscelot. Our customizations were based off of [SVN commit #2](http://projects.oscelot.org/gf/project/autosignon/scmsvn/?action=browse&path=%2Ftrunk&view=rev&revision=2) from that project.
## Modifications To-Date
- Customized the `src/main/webapp/access_denied.jsp` page to auto-redirect to the Bb Native login page in case there are issues with portal authentication
- Edited the `BB_FORWARD_HOME` and `BB_COURSE_HOME` constants in `src/java/com/blackboard/gs/autosignon/AutoSignonConfig.java` to reflect changes in the April 2014 release of Blackboard 9.1, specifically the removal of framesets

## Build Instructions
After making any modifications under the `src` directory that are needed, run the following command from the root of the project:
	
	./gradlew build

This will compile all the java files into classes and build a .war with the proper structure in `build/libs`


Last Updated 2014-05-21 @ 0908 by Kenny Barnt