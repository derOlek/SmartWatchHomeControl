package com.drejkim.androidwearmotionsensors;

import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PatternDetector {

    public static boolean detectWristFlickUp(Float[] rotValuesX, Float[] rotValuesY, Float[] rotValuesZ){

        //Avg berechnen
        //Tiefpunkt
        //Höhepunkt
        int largestValIndex = 0, smallestValIndex = 0;
        float avg = 0.0f;
        for(int i = 0; i < rotValuesX.length; i++){
            avg += rotValuesX[i];

            if(rotValuesX[i] > rotValuesX[largestValIndex]){
                largestValIndex = i;
            }
            if(rotValuesX[i] < rotValuesX[smallestValIndex]){
                smallestValIndex = i;
            }
        }

        avg = avg/rotValuesX.length;

        Log.d("Test", "Avg: " + avg +  " , Highest: " + rotValuesX[largestValIndex] +  " , Lowest: " + rotValuesX[smallestValIndex]);

        if(rotValuesX[smallestValIndex] < (avg - 10) && rotValuesX[largestValIndex] > (avg + 5)){
            int countNotRising = 0;
            float lastVal = rotValuesX[smallestValIndex];
            for(int i = smallestValIndex+1; i < largestValIndex; i++) {
                if (lastVal > rotValuesX[i]) {
                    countNotRising++;
                }
                lastVal = rotValuesX[i];
            }
                Log.d("Test", "None rising values: " + countNotRising);

            if(countNotRising / (largestValIndex - smallestValIndex * 1.0f) < 0.2f){

                float avgY = 0.0f;
                float sumY = 0.0f;
                for(int i = 0; i < rotValuesY.length; i++){
                    avgY += rotValuesY[i];
                }
                avgY = avgY/rotValuesY.length;

                float avgZ = 0.0f;
                float sumZ = 0.0f;
                for(int i = 0; i < rotValuesZ.length; i++){
                    avgZ += rotValuesZ[i];
                }
                avgZ = avgZ/rotValuesZ.length;

                //Calculate standard deviation
                for(int i = 0; i < rotValuesY.length; i++){
                    sumY += rotValuesY[i] - avgY;
                }
                for(int i = 0; i < rotValuesZ.length; i++){
                    sumZ += rotValuesZ[i] - avgZ;
                }

                float devY = (float) Math.sqrt(Math.pow(sumY, 2)/rotValuesY.length);
                float devZ = (float) Math.sqrt(Math.pow(sumZ, 2)/rotValuesZ.length);

                Log.d("Test", "Devaition Y: " + devY +  " Devaition Z: " + devZ);

                return Math.abs(devY) < 1 && Math.abs(devZ) < 1;
            }
        }


        return false;
    }

}
