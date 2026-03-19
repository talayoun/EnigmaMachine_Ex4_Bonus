git clone https://github.com/talayoun/EnigmaMachine_Ex4_Bonus.git ./enigma

cd enigma

call mvn clean install -DskipTests

start "Enigma Backend" java -Dgoogle.api.key=AIzaSyCUjFDoZAVuEWZtVvIiRbeN490sgI_mLqw -jar enigma-app/target/enigma-app-1.0-SNAPSHOT.jar

cd enigma-ui

call npm install

start "Enigma Frontend" npm start

echo Frontend: http://localhost:3000
