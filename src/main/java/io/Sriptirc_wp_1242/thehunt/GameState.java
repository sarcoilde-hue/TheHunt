package io.Sriptirc_wp_1242.thehunt;

public enum GameState {
    DISABLED,       // 插件未启用
    LOBBY_WAITING,  // 大厅等待中
    LOBBY_COUNTDOWN,// 大厅倒计时
    IN_PROGRESS,    // 游戏进行中
    ENDING          // 游戏结束中
}