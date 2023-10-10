// Author: Andrew Hu and Vincent Tunnell

package app.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class represents a single name instance. It is responsible for passing commands down to the individual recording
 * objects. it also ensures that the best recording for any given name is always played.
 * @author: Andrew Hu and Vincent Tunnell
 */
public class Name implements Comparable {
    private String _name;
    private ObservableList<Recording> _databaseRecordings = FXCollections.observableArrayList();
    private ObservableList<Recording> _userRecordings = FXCollections.observableArrayList();

    public Name(String name){ _name = name; }

    public String getName(){ return _name; }

    public void addUserRecording(Recording recording){ _userRecordings.add(0,recording); }

    public void addDatabaseRecording(Recording recording){ _databaseRecordings.add(recording); }

    /**
     * Overrides toString so name will be printed correctly by list view
     * @return Returns name followed by how many repeated entries in database there are
     */
    @Override
    public String toString(){
        // if theres only a single recording associated with a name, simply just display the name
        if (_databaseRecordings.size() == 1 || _databaseRecordings.size() == 0){
            return _name;
        } else {
            String name = _name + " (" + _databaseRecordings.size() + " recordings)";
            return name;
        }
    }

    /**
     * Plays the best recording in the list of database recordings
     */
    public void playRecording(){
        getBestRecording().playRecording();
    }

    /**
     * Given a recording object, this method searches the list of recordings in this object and deletes it. Also deletes
     * the file.
     * @param recording Recording the user wants to delete
     */
    public void removeUserRecording(Recording recording){
        // delete the recording object from this name object
        _userRecordings.remove(recording);
        // delete the actual file from system
        File file = new File(recording.getPath());
        if (!file.delete()) {
            System.out.println("Could not delete file " + recording.getPath());
        }
    }

    /**
     * Creates a custom user recording object using the current date and time of the system. Uses the same format as
     * the database recordings
     * @return The created recording object.
     */
    public Recording createRecordingObject(){
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        Date d = new Date();
        String dateAndTime = dateFormat.format(d);
        String[] dateAndTimeArray = dateAndTime.split(" ");

        String date = dateAndTimeArray[0];
        String time = dateAndTimeArray[1];
        String stringName = getName();


        String path = NamesModel.USER_RECORDINGS_DIRECTORY + "/se206_" + date + "_" + time + "_" + stringName + ".wav";

        String trimmedPath = NamesModel.TRIMMED_NORMALISED_DIRECTORY + "/" + path;

        return new Recording(stringName, date, path, trimmedPath, time);
    }

    /**
     * Creates an audio file in wav format based on a maximum 7 second capture of the microphone
     * @param recording The recording object that represents the name that will be recorded.
     * @return The process created by the ffmpeg command.
     */
    public Process record(Recording recording){

        String audioCommand = "ffmpeg -f alsa -i default -t " + NamesModel.MAX_RECORDING_SECS + " " + "./" + recording.getPath();
        BashCommand create = new BashCommand(audioCommand);
        create.startProcess();

        return create.getProcess();
    }

    public String getCleanName(){
        return getName();
    }

    public ObservableList<Recording> getUserRecordings(){
        return _userRecordings;
    }

    /**
     * Finds the best database recording in the list of names (determined by how many flags there are on each name)
     * @return the best database recording object
     */
    public Recording getBestRecording(){
        // loop through the all the recordings and find the one with the highest rating
        Recording bestRecording = _databaseRecordings.get(0);
        for (Recording recording : _databaseRecordings){
            if (recording.getBadRecordings() < bestRecording.getBadRecordings()){
                bestRecording = recording;
            }
        }
        return bestRecording;
    }

    // flags the current recording (best recording) as poor quality

    /**
     * Flags the current recording that is playing as poor quality
     * @return true indiciating the name has been successfully flagged
     */
    public boolean flagRecording(){
        getBestRecording().flagAsBad();
        // return true as single names can be flagged
        return true;
    }

    public float getRecordingLength(){
        return getBestRecording().getRecordingLength();
    }

    public void normaliseBestRecording(){
        getBestRecording().normaliseAndTrimAudioFile();
    }

    /**
     * Overriden compareTo method means we can sort the objects easily in lexographical order.
     * @param o The name object to be compared with this name object.
     * @return An int indicating the order of the two objects. (-1,0,1)
     */
    @Override
    public int compareTo(Object o) {
        Name name = (Name) o;
        return getName().compareTo(name.getName());
    }
}