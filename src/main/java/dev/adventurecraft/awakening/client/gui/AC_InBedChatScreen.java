package dev.adventurecraft.awakening.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.MultiplayerLocalPlayer;
import net.minecraft.locale.I18n;
import net.minecraft.network.packets.PlayerCommandPacket;

@Environment(EnvType.CLIENT)
public class AC_InBedChatScreen extends AC_ChatScreen {

    public void init() {
        super.init();

        String stopText = I18n.getInstance().get("multiplayer.stopSleeping");
        this.buttons.add(new Button(1, this.width / 2 - 100, this.height - 40, stopText));
    }

    protected void keyPressed(char eventCharacter, int eventKey) {
        if (eventKey == 1) {
            this.sendWakeUp();
        }
        else if (eventKey == 28) {
            this.submitMessage();
            this.messageBox.setValue("");
        }
        else {
            super.keyPressed(eventCharacter, eventKey);
        }
    }

    protected void buttonClicked(Button button) {
        if (button.id == 1) {
            this.sendWakeUp();
        }
        else {
            super.buttonClicked(button);
        }
    }

    private void sendWakeUp() {
        if (this.minecraft.player instanceof MultiplayerLocalPlayer player) {
            player.connection.send(new PlayerCommandPacket(player, 3));
        }
    }
}
