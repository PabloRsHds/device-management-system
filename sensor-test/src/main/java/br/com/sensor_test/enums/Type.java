package br.com.sensor_test.enums;

import lombok.Getter;

import java.util.Random;

@Getter
public enum Type {
    
    // Temperature & Humidity
    TEMPERATURE_SENSOR(-40f, 125f, Unit.CELSIUS),
    HUMIDITY_SENSOR(0f, 100f, Unit.PERCENTAGE),
    THERMOCOUPLE(-200f, 600f, Unit.CELSIUS),
    THERMISTOR(-50f, 150f, Unit.CELSIUS),

    // Gas & Air Quality
    GAS_SENSOR(0f, 1000f, Unit.PPM),
    CO2_SENSOR(400f, 5000f, Unit.PPM),
    CO_SENSOR(0f, 1000f, Unit.PPM),
    AIR_QUALITY_SENSOR(0f, 500f, Unit.PPM),
    VOC_SENSOR(0f, 500f, Unit.PPM),

    // Water & Liquid
    WATER_LEVEL_SENSOR(0f, 100f, Unit.PERCENTAGE),
    FLOW_SENSOR(0f, 1000f, Unit.LITER_PER_MINUTE),
    PH_SENSOR(0f, 14f, Unit.PERCENTAGE), // pH não tem unidade no seu enum
    TURBIDITY_SENSOR(0f, 1000f, Unit.PPM), // aproximação comum
    PRESSURE_SENSOR(0f, 300f, Unit.BAR),

    // Distance & Proximity
    DISTANCE_SENSOR(0f, 500f, Unit.METER),
    ULTRASONIC_SENSOR(2f, 400f, Unit.CENTIMETER),
    PROXIMITY_SENSOR(0f, 50f, Unit.CENTIMETER),
    LIDAR_SENSOR(0f, 1000f, Unit.METER),
    TOF_SENSOR(0f, 200f, Unit.CENTIMETER),

    // Motion & Position
    MOTION_SENSOR(0f, 1f, Unit.PERCENTAGE),
    ACCELEROMETER(-16f, 16f, Unit.METER_PER_SECOND),
    GYROSCOPE(-2000f, 2000f, Unit.METER_PER_SECOND),
    MAGNETOMETER(-100f, 100f, Unit.AMPERE), // aproximação
    IMU(-100f, 100f, Unit.METER_PER_SECOND),

    // Light
    LIGHT_SENSOR(0f, 100_000f, Unit.LUX),
    UV_SENSOR(0f, 11f, Unit.PERCENTAGE),
    AMBIENT_LIGHT_SENSOR(0f, 100_000f, Unit.LUX),

    // Sound
    SOUND_SENSOR(0f, 150f, Unit.DECIBEL),
    MICROPHONE(0f, 150f, Unit.DECIBEL),

    // Vibration & Force
    VIBRATION_SENSOR(0f, 100f, Unit.METER_PER_SECOND),
    FORCE_SENSOR(0f, 1000f, Unit.NEWTON),
    LOAD_CELL(0f, 5000f, Unit.KILOGRAM_FORCE),
    STRAIN_GAUGE(-1000f, 1000f, Unit.OHM), // aproximação elétrica

    // Security
    SMOKE_SENSOR(0f, 1000f, Unit.PPM),
    FLAME_SENSOR(0f, 1f, Unit.PERCENTAGE),
    PIR_SENSOR(0f, 1f, Unit.PERCENTAGE);

    private final float min;
    private final float max;
    private final Unit unit;

    Type(float min, float max, Unit unit) {
        this.min = min;
        this.max = max;
        this.unit = unit;
    }

    private static final Random random = new Random();
    private static final float TEST_MARGIN = 100f;

    public float randomMinLimit(float minLimit, float maxLimit) {

        float lower = minLimit - TEST_MARGIN;
        float upper = Math.min(minLimit + TEST_MARGIN, maxLimit);

        return random.nextFloat(lower, upper);
    }

    public float randomMaxLimit(float minLimit, float maxLimit) {

        float lower = Math.max(maxLimit - TEST_MARGIN, minLimit);
        float upper = maxLimit + TEST_MARGIN;

        return random.nextFloat(lower, upper);
    }
}
