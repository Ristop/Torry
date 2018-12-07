package performance_testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PerformanceTest {

    private long start;
    private long end;
    private String filePath;
    private boolean timerAlreadyStarted = false;
    private int iterations_nr;
    private String file_to_test;


    public PerformanceTest(String filePath, String file_to_test){
        this.filePath = filePath;
        this.iterations_nr = 100;
        this.file_to_test = file_to_test;
    }

    public PerformanceTest(String filePath, String file_to_test,  int iterations_nr){
        this.filePath = filePath;
        this.iterations_nr = iterations_nr;
        this.file_to_test = file_to_test;
    }

    public void startClockIfNotStarted(){
        if(!timerAlreadyStarted){
            start = System.currentTimeMillis();
            timerAlreadyStarted = true;
        }
    }

    public void stopClock(){
        end = System.currentTimeMillis();
    }

    public int getIterations_nr() {
        return iterations_nr;
    }

    public void writeTimeToFile(){

        File file = new File(filePath);

        try(
               FileWriter fr = new FileWriter(file, true);
               BufferedWriter br = new BufferedWriter(fr);
        ){
            br.write(end-start+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        iterations_nr--;
        timerAlreadyStarted = false;
    }

    public void deleteFile(){
        File file = new File(file_to_test);
        file.delete();
    }
}
