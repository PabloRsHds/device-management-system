package br.com.device_management.enums;

public enum Type {

    // Temperature & Humidity
    TEMPERATURE_SENSOR,
    HUMIDITY_SENSOR,
    THERMOCOUPLE,
    THERMISTOR,

    // Gas & Air Quality
    GAS_SENSOR,
    CO2_SENSOR,
    CO_SENSOR,
    AIR_QUALITY_SENSOR,
    VOC_SENSOR,

    // Water & Liquid
    WATER_LEVEL_SENSOR,
    FLOW_SENSOR,
    PH_SENSOR,
    TURBIDITY_SENSOR,
    PRESSURE_SENSOR,

    // Distance & Proximity
    DISTANCE_SENSOR,
    ULTRASONIC_SENSOR,
    PROXIMITY_SENSOR,
    LIDAR_SENSOR,
    TOF_SENSOR,

    // Motion & Position
    MOTION_SENSOR,
    ACCELEROMETER,
    GYROSCOPE,
    MAGNETOMETER,
    IMU,

    // Light
    LIGHT_SENSOR,
    UV_SENSOR,
    AMBIENT_LIGHT_SENSOR,

    // Sound
    SOUND_SENSOR,
    MICROPHONE,

    // Vibration & Force
    VIBRATION_SENSOR,
    FORCE_SENSOR,
    LOAD_CELL,
    STRAIN_GAUGE,

    // Security
    SMOKE_SENSOR,
    FLAME_SENSOR,
    PIR_SENSOR;

}
