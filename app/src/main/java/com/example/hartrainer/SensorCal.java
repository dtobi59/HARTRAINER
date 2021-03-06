package com.example.hartrainer;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.SENSOR_SERVICE;



public class SensorCal {

        public final int WRITE_PERMISSON_REQUEST_CODE =1;

        // angular speeds from gyro
        private float[] gyro = new float[3];

        // rotation matrix from gyro data
        private float[] gyroMatrix = new float[9];

        // orientation angles from gyro matrix
        private float[] gyroOrientation = new float[3];

        // magnetic field vector
        public float[] magnet = new float[3];

        // accelerometer vector
        public float[] accel = new float[3];

        // orientation angles from accel and magnet
        public float[] accMagOrientation = new float[3];

        // final orientation angles from sensor fusion
        private float[] fusedOrientation = new float[3];


        String sensor_values = "";

        public SensorCal() {

            gyroOrientation[0] = 0.0f;
            gyroOrientation[1] = 0.0f;
            gyroOrientation[2] = 0.0f;

            // initialise gyroMatrix with identity matrix
            gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
            gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
            gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;

            // get sensorManager and initialise sensor listeners

            // wait for one second until gyroscope and magnetometer/accelerometer
            // data is initialised then scedule the complementary filter task
            fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                    1000, TIME_CONSTANT);
        }


        // accelerometer and magnetometer based rotation matrix
        private float[] rotationMatrix = new float[9];




        public float[] calculateAccMagOrientation() {
            float[] data = new float[3];
            if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
                data = SensorManager.getOrientation(rotationMatrix, accMagOrientation);
                //System.out.println("Sensor(AccMagOrientation):"+Arrays.toString(data));

            }

            return data;
        }

        public static final float EPSILON = 0.000000001f;

        private void getRotationVectorFromGyro(float[] gyroValues,
                                               float[] deltaRotationVector,
                                               float timeFactor)
        {
            float[] normValues = new float[3];

            // Calculate the angular speed of the sample
            float omegaMagnitude =
                    (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                            gyroValues[1] * gyroValues[1] +
                            gyroValues[2] * gyroValues[2]);

            // Normalize the rotation vector if it's big enough to get the axis
            if(omegaMagnitude > EPSILON) {
                normValues[0] = gyroValues[0] / omegaMagnitude;
                normValues[1] = gyroValues[1] / omegaMagnitude;
                normValues[2] = gyroValues[2] / omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * timeFactor;
            float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
            deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
            deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
            deltaRotationVector[3] = cosThetaOverTwo;
        }

        private static final float NS2S = 1.0f / 1000000000.0f;
        private float timestamp;
        private boolean initState = true;

        public float[] gyroFunction(SensorEvent event) {
            // don't start until first accelerometer/magnetometer orientation has been acquired
            float[] data = new float[3];
            if (accMagOrientation == null)
                return data;

            // initialisation of the gyroscope based rotation matrix
            if(initState) {
                float[] initMatrix = new float[9];
                initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
                float[] test = new float[3];
                SensorManager.getOrientation(initMatrix, test);
                gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
                initState = false;
            }

            // copy the new gyro values into the gyro array
            // convert the raw gyro data into a rotation vector
            float[] deltaVector = new float[4];
            if(timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2S;
                System.arraycopy(event.values, 0, gyro, 0, 3);
                getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
            }

            // measurement done, save current time for next interval
            timestamp = event.timestamp;

            // convert rotation vector into rotation matrix
            float[] deltaMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

            // apply the new rotation interval on the gyroscope based rotation matrix
            gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

            // get the gyroscope based orientation from the rotation matrix
            data = SensorManager.getOrientation(gyroMatrix, gyroOrientation);
            ///System.out.println("Sensor(Gyroscope):"+ Arrays.toString(data));

//            long timeInMillis = (new Date()).getTime()
//                    + (event.timestamp - System.nanoTime()) / 1000000L;
//
//            sensor_values += "Dave*"+"sitdown"+"*"+timeInMillis+"*";
//            sensor_values += accel[0]+"*"+accel[1]+"*"+accel[2]+"*";
//            sensor_values += data[0]+"*"+data[1]+"*"+data[2]+";\n";
//            System.out.println(sensor_values);

            return data;

        }

        private float[] getRotationMatrixFromOrientation(float[] o) {
            float[] xM = new float[9];
            float[] yM = new float[9];
            float[] zM = new float[9];

            float sinX = (float)Math.sin(o[1]);
            float cosX = (float)Math.cos(o[1]);
            float sinY = (float)Math.sin(o[2]);
            float cosY = (float)Math.cos(o[2]);
            float sinZ = (float)Math.sin(o[0]);
            float cosZ = (float)Math.cos(o[0]);

            // rotation about x-axis (pitch)
            xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
            xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
            xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

            // rotation about y-axis (roll)
            yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
            yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
            yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

            // rotation about z-axis (azimuth)
            zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
            zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
            zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

            // rotation order is y, x, z (roll, pitch, azimuth)
            float[] resultMatrix = matrixMultiplication(xM, yM);
            resultMatrix = matrixMultiplication(zM, resultMatrix);
            return resultMatrix;
        }

        private float[] matrixMultiplication(float[] A, float[] B) {
            float[] result = new float[9];

            result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
            result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
            result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

            result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
            result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
            result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

            result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
            result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
            result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

            return result;
        }

        public static final int TIME_CONSTANT = 30;
        public static final float FILTER_COEFFICIENT = 0.98f;
        private Timer fuseTimer = new Timer();


        class calculateFusedOrientationTask extends TimerTask {
            public void run() {
                float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
                fusedOrientation[0] =
                        FILTER_COEFFICIENT * gyroOrientation[0]
                                + oneMinusCoeff * accMagOrientation[0];

                fusedOrientation[1] =
                        FILTER_COEFFICIENT * gyroOrientation[1]
                                + oneMinusCoeff * accMagOrientation[1];

                fusedOrientation[2] =
                        FILTER_COEFFICIENT * gyroOrientation[2]
                                + oneMinusCoeff * accMagOrientation[2];

                // overwrite gyro matrix and orientation with fused orientation
                // to comensate gyro drift
                gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
                System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
            }
        }
    }


