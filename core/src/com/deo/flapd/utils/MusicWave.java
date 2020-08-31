package com.deo.flapd.utils;

/*

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

import static com.deo.flapd.utils.DUtils.concatArray;

public class MusicWave {

    private short[] samples;
    private float[] averageChannelAmplitude;
    private AudioDevice device;

    public MusicWave() {

        String path;

        device = Gdx.audio.newAudioDevice(44100, true);

        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            path = "Android/data/!DeltaCore/music.mp3";
        } else {
            path = "!DeltaCore/music.mp3";
        }

        File file = Gdx.files.external(path).file();

        try {

            boolean done = false;

            short[] pcmOut = new short[]{};

            InputStream data = new FileInputStream(file.getPath());
            Bitstream bitStream = new Bitstream(data);
            Decoder decoder = new Decoder();
            decoder.getOutputChannels();
            while (!done) {
                Header frameHeader = bitStream.readFrame();
                if (frameHeader == null) {
                    done = true;
                } else {
                    SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitStream);
                    short[] next = output.getBuffer();
                    pcmOut = concatArray(pcmOut, next);
                }
                bitStream.closeFrame();
            }

            short[] samples1ch = new short[pcmOut.length/2];

            for(int i = 0; i<samples1ch.length/2; i+=2){
                samples1ch[i/2] = (short)((pcmOut[i] + pcmOut[i+1])/2f);
            }

            samples = samples1ch;
            averageChannelAmplitude = new float[samples.length];
            for(int i = 0; i<samples.length; i++){
                averageChannelAmplitude[i] = samples[i];
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getSixteenBitSample(int high, int low) {
        return (high << 8) + (low & 0x00ff);
    }

    private float[][] getUnscaledAmplitude(short[] eightBitByteArray, int nbChannels) {

        float[][] toReturn = new float[nbChannels][eightBitByteArray.length / (2 * nbChannels)];
        int index = 0;

        for (int audioByte = 0; audioByte < eightBitByteArray.length; ) {
            for (int channel = 0; channel < nbChannels; channel++) {
                int low = eightBitByteArray[audioByte];
                audioByte++;
                int high = eightBitByteArray[audioByte];
                audioByte++;
                int sample = getSixteenBitSample(high, low);

                toReturn[channel][index] = sample;
            }
            index++;
        }

        return toReturn;
    }

    public float[] getAmplitude() {
        return averageChannelAmplitude;
    }

    public short[] getSamples() {
        return samples;
    }

    public AudioDevice getMusic() {
        return device;
    }

    public void dispose() {
        device.dispose();
    }
}

 */
