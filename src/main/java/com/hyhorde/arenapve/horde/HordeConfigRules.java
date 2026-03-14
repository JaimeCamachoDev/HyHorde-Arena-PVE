package com.hyhorde.arenapve.horde;

final class HordeConfigRules {
    static final int MIN_ROUNDS = 1;
    static final int MAX_ROUNDS = 200;
    static final int MIN_ENEMIES_PER_ROUND = 1;
    static final int MAX_ENEMIES_PER_ROUND = 400;
    static final int MIN_ENEMY_INCREMENT = 0;
    static final int MAX_ENEMY_INCREMENT = 400;
    static final int MIN_PLAYER_MULTIPLIER = 1;
    static final int MAX_PLAYER_MULTIPLIER = 20;
    static final int MIN_WAVE_DELAY_SECONDS = 0;
    static final int MAX_WAVE_DELAY_SECONDS = 300;
    static final int MIN_REWARD_ITEM_QUANTITY = 1;
    static final int MAX_REWARD_ITEM_QUANTITY = 100;
    static final int MIN_ENEMY_LEVEL = 1;
    static final int MAX_ENEMY_LEVEL = 200;
    static final double MIN_RADIUS = 1.0;
    static final double MAX_RADIUS = 128.0;
    static final double MIN_ARENA_JOIN_RADIUS = 4.0;
    static final double MAX_ARENA_JOIN_RADIUS = 512.0;

    static final double DEFAULT_SPAWN_X = 0.0;
    static final double DEFAULT_SPAWN_Y = 64.0;
    static final double DEFAULT_SPAWN_Z = 0.0;
    static final double DEFAULT_MIN_RADIUS = 5.0;
    static final double DEFAULT_MAX_RADIUS = 12.0;
    static final double DEFAULT_ARENA_JOIN_RADIUS = 32.0;
    static final int DEFAULT_ROUNDS = 5;
    static final int DEFAULT_BASE_ENEMIES = 10;
    static final int DEFAULT_ENEMIES_INCREMENT = 3;
    static final int DEFAULT_WAVE_DELAY_SECONDS = 8;
    static final int DEFAULT_PLAYER_MULTIPLIER = 1;
    static final int DEFAULT_REWARD_EVERY_ROUNDS = 2;
    static final int DEFAULT_REWARD_ITEM_QUANTITY = 1;
    static final int DEFAULT_ENEMY_LEVEL_MIN = 1;
    static final int DEFAULT_ENEMY_LEVEL_MAX = 1;

    private HordeConfigRules() {
    }
}
