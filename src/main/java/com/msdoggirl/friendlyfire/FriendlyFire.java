package com.msdoggirl.friendlyfire;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod("friendlyfire")
public class FriendlyFire {

    private static List<String> protectedMobs = new ArrayList<>();

    public FriendlyFire() {
        // Load configuration file using Path.of()
        ConfigHandler.load(Path.of("config/friendlyfire.toml"));
        protectedMobs = ConfigHandler.getProtectedMobs();

        // Register event listeners
        MinecraftForge.EVENT_BUS.register(this);

        // Add default mobs if the list is empty
        if (protectedMobs.isEmpty()) {
            ConfigHandler.setProtectedMobs(protectedMobs); // Save defaults to config
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player) {
            String mobID = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString();

            if (protectedMobs.contains(mobID)) {
                event.setCanceled(true);
            }
        }
    }

    public static void addProtectedMob(String mobID) {
        if (!protectedMobs.contains(mobID)) {
            protectedMobs.add(mobID);
            ConfigHandler.setProtectedMobs(protectedMobs);
        }
    }

    public static void removeProtectedMob(String mobID) {
        protectedMobs.remove(mobID);
        ConfigHandler.setProtectedMobs(protectedMobs);
    }

    public static List<String> getProtectedMobs() {
        return protectedMobs;
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientEvents {
    
        @SubscribeEvent
        public static void onScreenPostInit(ScreenEvent.Init.Post event) {
            // Check if the current screen is the InventoryScreen
            if (event.getScreen() instanceof InventoryScreen inventoryScreen) {
                // Center the button horizontally under the inventory window
                int buttonX = inventoryScreen.getGuiLeft() + (inventoryScreen.getXSize() / 2) - 75; // Adjust for button width
                int buttonY = inventoryScreen.getGuiTop() + inventoryScreen.getYSize() + 10; // Position below the window
        
                // Add the button with the new label
                event.addListener(new Button.Builder(Component.literal("Add Mob to Friendly Fire"), button -> {
                    Minecraft.getInstance().setScreen(new MobInputScreen());
                }).bounds(buttonX, buttonY, 150, 20).build());
            }
        }
        
    }
    

    public static class MobInputScreen extends Screen {

        private EditBox mobInputBox; // Text box for entering mob ID
        private int scrollOffset = 0; // Offset for scrolling through mob list

        protected MobInputScreen() {
            super(Component.literal("Mob Input"));
        }

        @Override
        protected void init() {
            super.init();
        
            mobInputBox = new EditBox(this.font, this.width / 2 - 100, 20, 200, 20, Component.literal("Enter Mob ID"));
            this.addRenderableWidget(mobInputBox);
        
            this.addRenderableWidget(new Button.Builder(Component.literal("Save"), button -> {
                // Save the entered mob ID to the protected list
                String mobID = mobInputBox.getValue();
                if (!mobID.isEmpty() && !protectedMobs.contains(mobID)) {
                    protectedMobs.add(mobID);
                    ConfigHandler.setProtectedMobs(protectedMobs); // Save updated list to config
                }
                mobInputBox.setValue(""); // Clear the input field
            }).bounds(this.width / 2 + 110, 20, 60, 20).build());
        
        
            // Add scrolling functionality
            this.addRenderableWidget(new Button.Builder(Component.literal("Scroll Down"), button -> {
                scrollOffset = Math.min(scrollOffset + 5, Math.max(0, protectedMobs.size() - 5)); // Scroll down
                this.clearWidgets(); // Clear widgets for re-rendering
                this.init(); // Refresh the screen
            }).bounds(this.width / 2 + 60, this.height / 2 + 80, 100, 20).build());
        
            this.addRenderableWidget(new Button.Builder(Component.literal("Scroll Up"), button -> {
                scrollOffset = Math.max(scrollOffset - 5, 0); // Scroll up
                this.clearWidgets(); // Clear widgets for re-rendering
                this.init(); // Refresh the screen
            }).bounds(this.width / 2 - 160, this.height / 2 + 80, 100, 20).build());
        }
        
        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            super.render(guiGraphics, mouseX, mouseY, delta);
        
            // Render the title of the protected mobs list at the top
            guiGraphics.drawCenteredString(this.font, "Protected Mobs:", this.width / 2, 50, 0xFFFFFF);
        
            // Render the list of mobs with scrolling
            for (int j = scrollOffset; j < Math.min(protectedMobs.size(), scrollOffset + 5); j++) {
                int yOffset = 70 + (j - scrollOffset) * 20; // Positioning mobs below the title
                String mobID = protectedMobs.get(j);
        
                // Display the mob ID
                guiGraphics.drawString(this.font, mobID, this.width / 2 - 100, yOffset, 0xFFFFFF);
        
                // Add a "Remove" button next to each mob
                this.addRenderableWidget(new Button.Builder(Component.literal("Remove"), button -> {
                    protectedMobs.remove(mobID); // Remove the mob from the list
                    ConfigHandler.setProtectedMobs(protectedMobs); // Save updated list to the config
                    this.clearWidgets(); // Clear current widgets
                    this.init(); // Refresh the screen to reflect the updated list
                }).bounds(this.width / 2 + 50, yOffset, 80, 20).build());
            }
        }
        
        
        

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(null);
        }
    }
}
