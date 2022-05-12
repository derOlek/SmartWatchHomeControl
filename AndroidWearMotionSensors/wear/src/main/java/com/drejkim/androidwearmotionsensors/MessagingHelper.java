package com.drejkim.androidwearmotionsensors;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;

public class MessagingHelper {

    private static final String VOICE_TRANSCRIPTION_CAPABILITY_NAME = "voice_transcription";
    public static final String VOICE_TRANSCRIPTION_MESSAGE_PATH = "/voice_transcription";

    public MessagingHelper(final Context context) throws ExecutionException, InterruptedException {
        setupVoiceTranscription(context);
    }


    private static void setupVoiceTranscription(Context context) throws ExecutionException, InterruptedException {
        CapabilityInfo capabilityInfo = Tasks.await(
                Wearable.getCapabilityClient(context).getCapability(
                        VOICE_TRANSCRIPTION_CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE));
        //Log.d("Test", "Nodes:");
        for (Node n : capabilityInfo.getNodes()) {
            Log.d("Test", n.getDisplayName());
        }
        // capabilityInfo has the reachable nodes with the transcription capability
        updateTranscriptionCapability(capabilityInfo);
    }

    public static String transcriptionNodeId = null;

    private static void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();

        transcriptionNodeId = pickBestNodeId(connectedNodes);
    }

    private static String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    public static void requestTranscription(final Context context, String data) {
        if(transcriptionNodeId == null){
            try {
                setupVoiceTranscription(context);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (transcriptionNodeId != null) {
            byte[] byteData = data.getBytes(StandardCharsets.UTF_8);
            //Log.d("Test", "Sending (RAW): " + Arrays.toString(byteData));

            Task<Integer> sendTask = Wearable.getMessageClient(context).sendMessage(transcriptionNodeId, VOICE_TRANSCRIPTION_MESSAGE_PATH, byteData);
            sendTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
