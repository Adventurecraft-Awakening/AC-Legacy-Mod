package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class AC_Blocks {

    public static final Block lockedDoor = (new AC_BlockLockedDoor(150, 208, AC_Items.doorKey.id)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("lockedDoor");
    public static final Block lockedBossDoor = (new AC_BlockLockedDoor(156, 210, AC_Items.bossKey.id)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("lockedBossDoor");
    public static final Block newMobSpawner = (new AC_BlockMobSpawner(151, 65)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("mobSpawner2");
    public static final Block spawnBlock = (new AC_BlockSpawn(152, 0)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("spawn");
    public static final AC_BlockTrigger triggerBlock = (AC_BlockTrigger) (new AC_BlockTrigger(153, 1)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("trigger");
    public static final Block triggerDoor = (new AC_BlockTriggeredDoor(154)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("triggeredDoor");
    public static final Block spikeBlock = (new AC_BlockSpike(155)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("spike");
    public static final AC_BlockTriggerInverter triggerInverter = (AC_BlockTriggerInverter) (new AC_BlockTriggerInverter(157, 2)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("triggerInverter");
    public static final AC_BlockTriggerMemory triggerMemory = (AC_BlockTriggerMemory) (new AC_BlockTriggerMemory(158, 3)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("triggerMemory");
    public static final AC_BlockClip clipBlock = (AC_BlockClip) (new AC_BlockClip(159, 4, Material.AIR)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("clip");
    public static final AC_BlockRedstoneTrigger redstoneTrigger = (AC_BlockRedstoneTrigger) (new AC_BlockRedstoneTrigger(160, 228)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("redstoneTrigger");
    public static final AC_BlockRedstonePower redstonePower = (AC_BlockRedstonePower) (new AC_BlockRedstonePower(161, 185)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("redstonePower");
    public static final AC_BlockBombable bombableCobblestone = (AC_BlockBombable) (new AC_BlockBombable(162, 166, Material.STONE)).setHardness(2.0F).setBlastResistance(10.0F).setSounds(Block.PISTON_SOUNDS).setTranslationKey("crackedStonebrick");
    public static final AC_BlockBombable bombableStone = (AC_BlockBombable) (new AC_BlockBombable(163, 167, Material.STONE)).setHardness(1.5F).setBlastResistance(10.0F).setSounds(Block.PISTON_SOUNDS).setTranslationKey("crackedStone");
    public static final AC_BlockWeather weather = (AC_BlockWeather) (new AC_BlockWeather(164, 5)).setHardness(1.5F).setBlastResistance(10.0F).setSounds(Block.PISTON_SOUNDS).setTranslationKey("weather");
    public static final AC_BlockMusic musicTriggered = (AC_BlockMusic) (new AC_BlockMusic(165, 9)).setHardness(1.5F).setBlastResistance(10.0F).setSounds(Block.PISTON_SOUNDS).setTranslationKey("music");
    public static final Block pushableBlock = (new AC_BlockPushable(166, 212, Material.STONE)).setHardness(2.0F).setBlastResistance(10.0F).setSounds(Block.PISTON_SOUNDS).setTranslationKey("pushable");
    public static final AC_BlockTimer timer = (AC_BlockTimer) (new AC_BlockTimer(167, 8)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("timer");
    public static final AC_BlockMessage message = (AC_BlockMessage) (new AC_BlockMessage(168, 7)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("message");
    public static final Block fan = (new AC_BlockFan(169, 184, true)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("fan");
    public static final Block camera = (new AC_BlockCamera(170, 6)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("camera");
    public static final Block lightBulb = (new AC_BlockLightBulb(171, 14)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("lightBulb");
    public static final Block fanOff = (new AC_BlockFan(172, 200, false)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("fan");
    public static final Block script = (new AC_BlockScript(173, 15)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("script");
    public static final Block store = (new AC_BlockStore(174, 49)).setHardness(5.0F).setSounds(Block.GLASS_SOUNDS).setTranslationKey("store");
    public static final Block effect = (new AC_BlockEffect(175, 244)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("effect");
    public static final Block darkness = (new AC_BlockDarkness(200, 10)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("darkness");
    public static final Block triggerPushable = (new AC_BlockTriggerPushable(201, 213)).setHardness(2.0F).setBlastResistance(10.0F).setSounds(Block.PISTON_SOUNDS).setTranslationKey("triggerPushable");
    public static final Block storage = (new AC_BlockStorage(202, 11)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("storage");
    public static final Block healDamage = (new AC_BlockHealDamage(203, 12)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("healDamage");
    public static final Block teleport = (new AC_BlockTeleport(204, 13)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("teleport");
    public static final Block url = (new AC_BlockUrl(176, 245)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("url");
    public static final Block npcPath = (new AC_BlockNpcPath(177, 247)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("NPC Path Block");
    public static final Block pillarStone = (new AC_BlockPillar(205, 32)).setHardness(5.0F).setSounds(Block.PISTON_SOUNDS).setTranslationKey("pillarStone");
    public static final Block pillarMetal = (new AC_BlockPillar(206, 80)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("pillarMetal");
    public static final Block plant1 = (new AC_BlockPlant(207, 112)).setHardness(5.0F).setSounds(Block.GRASS_SOUNDS).setTranslationKey("flower");
    public static final Block trees = (new AC_BlockTree(208, 128)).setHardness(5.0F).setSounds(Block.GRASS_SOUNDS).setTranslationKey("sapling");
    public static final Block glassBlocks = (new AC_BlockTransparent(209, 144)).setHardness(5.0F).setSounds(Block.GLASS_SOUNDS).setTranslationKey("glass");
    public static final Block cageBlocks = (new AC_BlockTransparent(210, 160)).setHardness(5.0F).setSounds(Block.GLASS_SOUNDS).setTranslationKey("cage");
    public static final Block stoneBlocks1 = (new AC_BlockSolid(211, 176)).setHardness(5.0F).setSounds(Block.PISTON_SOUNDS).setTranslationKey("stone");
    public static final Block stoneBlocks2 = (new AC_BlockSolid(212, 192)).setHardness(5.0F).setSounds(Block.PISTON_SOUNDS).setTranslationKey("stone");
    public static final Block stoneBlocks3 = (new AC_BlockSolid(213, 208)).setHardness(5.0F).setSounds(Block.PISTON_SOUNDS).setTranslationKey("stone");
    public static final Block woodBlocks = (new AC_BlockSolid(214, 224)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("wood");
    public static final Block halfSteps1 = (new AC_BlockHalfStep(215, 240)).setHardness(5.0F).setSounds(Block.PISTON_SOUNDS).setTranslationKey("halfStep");
    public static final Block halfSteps2 = (new AC_BlockHalfStep(216, 0)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("halfStep");
    public static final Block halfSteps3 = (new AC_BlockHalfStep(217, 16)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("halfStepWood");
    public static final Block tableBlocks = (new AC_BlockTable(218, 32)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("table");
    public static final Block chairBlocks1 = (new AC_BlockChair(219, 64)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("chair");
    public static final Block chairBlocks2 = (new AC_BlockChair(220, 68)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("chair");
    public static final Block chairBlocks3 = (new AC_BlockChair(221, 64)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("chair");
    public static final Block chairBlocks4 = (new AC_BlockChair(222, 64)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("chair");
    public static final Block ropes1 = (new AC_BlockRope(223, 96)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("rope");
    public static final Block ropes2 = (new AC_BlockRope(224, 101)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("rope");
    public static final Block chains = (new AC_BlockChain(225, 106)).setHardness(5.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("chain");
    public static final Block ladders1 = (new AC_BlockLadderSubtypes(226, 112)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("ladder");
    public static final Block ladders2 = (new AC_BlockLadderSubtypes(227, 116)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("ladder");
    public static final Block ladders3 = (new AC_BlockLadderSubtypes(228, 120)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("ladder");
    public static final Block ladders4 = (new AC_BlockLadderSubtypes(229, 124)).setHardness(5.0F).setSounds(Block.WOOD_SOUNDS).setTranslationKey("ladder");
    public static final Block lights1 = (new AC_BlockPlant(230, 128)).setHardness(5.0F).setLightEmittance(15.0F / 16.0F).setSounds(Block.METAL_SOUNDS).setTranslationKey("torch");
    public static final Block plant2 = (new AC_BlockTree(231, 144)).setHardness(5.0F).setSounds(Block.GRASS_SOUNDS).setTranslationKey("flower");
    public static final Block plant3 = (new AC_BlockTree(232, 160)).setHardness(5.0F).setSounds(Block.GRASS_SOUNDS).setTranslationKey("flower");
    public static final Block overlay1 = (new AC_BlockOverlay(233, 176)).setHardness(5.0F).setSounds(Block.GRASS_SOUNDS).setTranslationKey("overlay");
    public static final Block stairs1 = (new AC_BlockStairMulti(234, Block.WOOD, 192)).setTranslationKey("stairs");
    public static final Block stairs2 = (new AC_BlockStairMulti(235, Block.WOOD, 196)).setTranslationKey("stairs");
    public static final Block stairs3 = (new AC_BlockStairMulti(236, Block.COBBLESTONE, 200)).setTranslationKey("stairs");
    public static final Block stairs4 = (new AC_BlockStairMulti(237, Block.COBBLESTONE, 204)).setTranslationKey("stairs");
    public static final Block slopes1 = (new AC_BlockSlope(238, Block.WOOD, 192)).setTranslationKey("slopes");
    public static final Block slopes2 = (new AC_BlockSlope(239, Block.WOOD, 196)).setTranslationKey("slopes");
    public static final Block slopes3 = (new AC_BlockSlope(240, Block.COBBLESTONE, 200)).setTranslationKey("slopes");
    public static final Block slopes4 = (new AC_BlockSlope(241, Block.COBBLESTONE, 204)).setTranslationKey("slopes");

    public static void convertACVersion(byte[] blocks) {
        if (blocks == null) {
            return;
        }

        for (int i = 0; i < blocks.length; ++i) {
            int id = ExChunk.translate256(blocks[i]);
            if (id >= 100 && id <= 122) {
                blocks[i] = (byte) ExChunk.translate128(id + 50);
            } else if (id >= 152 && id <= 155) {
                blocks[i] = (byte) ExChunk.translate128(id + 21);
            }
        }
    }

    static {
        ((ExBlock) lockedDoor).setTextureNum(3);
        ((ExBlock) lockedBossDoor).setTextureNum(3);
        ((ExBlock) spawnBlock).setTextureNum(2);
        ((ExBlock) triggerBlock).setTextureNum(2);
        ((ExBlock) triggerDoor).setTextureNum(3);
        ((ExBlock) spikeBlock).setTextureNum(3);
        ((ExBlock) triggerInverter).setTextureNum(2);
        ((ExBlock) triggerMemory).setTextureNum(2);
        ((ExBlock) clipBlock).setTextureNum(2);
        ((ExBlock) redstoneTrigger).setTextureNum(3);
        ((ExBlock) weather).setTextureNum(2);
        ((ExBlock) musicTriggered).setTextureNum(2);
        ((ExBlock) pushableBlock).setTextureNum(3);
        ((ExBlock) timer).setTextureNum(2);
        ((ExBlock) message).setTextureNum(2);
        ((ExBlock) camera).setTextureNum(2);
        ((ExBlock) lightBulb).setTextureNum(2);
        ((ExBlock) script).setTextureNum(2);
        ((ExBlock) script).setTextureNum(2);
        ((ExBlock) effect).setTextureNum(3);
        ((ExBlock) darkness).setTextureNum(2);
        ((ExBlock) triggerPushable).setTextureNum(3);
        ((ExBlock) storage).setTextureNum(2);
        ((ExBlock) healDamage).setTextureNum(2);
        ((ExBlock) teleport).setTextureNum(2);
        ((ExBlock) url).setTextureNum(3);
        ((ExBlock) npcPath).setTextureNum(3);
        ((ExBlock) pillarStone).setTextureNum(2);
        ((ExBlock) pillarMetal).setTextureNum(2);
        ((ExBlock) plant1).setTextureNum(2);
        ((ExBlock) trees).setTextureNum(2);
        ((ExBlock) glassBlocks).setTextureNum(2);
        ((ExBlock) cageBlocks).setTextureNum(2);
        ((ExBlock) stoneBlocks1).setTextureNum(2);
        ((ExBlock) stoneBlocks2).setTextureNum(2);
        ((ExBlock) stoneBlocks3).setTextureNum(2);
        ((ExBlock) woodBlocks).setTextureNum(2);
        ((ExBlock) halfSteps1).setTextureNum(2);
        ((ExBlock) halfSteps2).setTextureNum(3);
        ((ExBlock) halfSteps3).setTextureNum(3);
        ((ExBlock) tableBlocks).setTextureNum(3);
        ((ExBlock) chairBlocks1).setTextureNum(3);
        ((ExBlock) chairBlocks2).setTextureNum(3);
        ((ExBlock) chairBlocks3).setTextureNum(3);
        ((ExBlock) chairBlocks4).setTextureNum(3);
        ((ExBlock) ropes1).setTextureNum(3);
        ((ExBlock) ropes2).setTextureNum(3);
        ((ExBlock) chains).setTextureNum(3);
        ((ExBlock) ladders1).setTextureNum(3);
        ((ExBlock) ladders2).setTextureNum(3);
        ((ExBlock) ladders3).setTextureNum(3);
        ((ExBlock) ladders4).setTextureNum(3);
        ((ExBlock) lights1).setTextureNum(3);
        ((ExBlock) plant2).setTextureNum(3);
        ((ExBlock) plant3).setTextureNum(3);
        ((ExBlock) overlay1).setTextureNum(3);
        ((ExBlock) stairs1).setTextureNum(3);
        ((ExBlock) stairs2).setTextureNum(3);
        ((ExBlock) stairs3).setTextureNum(3);
        ((ExBlock) stairs4).setTextureNum(3);
        ((ExBlock) slopes1).setTextureNum(3);
        ((ExBlock) slopes2).setTextureNum(3);
        ((ExBlock) slopes3).setTextureNum(3);
        ((ExBlock) slopes4).setTextureNum(3);

        ((ExBlock) pillarStone).setSubTypes(16);
        ((ExBlock) pillarMetal).setSubTypes(16);
        ((ExBlock) plant1).setSubTypes(16);
        ((ExBlock) trees).setSubTypes(16);
        ((ExBlock) glassBlocks).setSubTypes(16);
        ((ExBlock) cageBlocks).setSubTypes(10);
        ((ExBlock) stoneBlocks1).setSubTypes(16);
        ((ExBlock) stoneBlocks2).setSubTypes(16);
        ((ExBlock) stoneBlocks3).setSubTypes(16);
        ((ExBlock) woodBlocks).setSubTypes(16);
        ((ExBlock) halfSteps1).setSubTypes(16);
        ((ExBlock) halfSteps2).setSubTypes(16);
        ((ExBlock) halfSteps3).setSubTypes(16);
        ((ExBlock) tableBlocks).setSubTypes(16);
        ((ExBlock) chairBlocks1).setSubTypes(16);
        ((ExBlock) chairBlocks2).setSubTypes(16);
        ((ExBlock) chairBlocks3).setSubTypes(16);
        ((ExBlock) chairBlocks4).setSubTypes(16);
        ((ExBlock) ropes1).setSubTypes(15);
        ((ExBlock) ropes2).setSubTypes(15);
        ((ExBlock) chains).setSubTypes(9);
        ((ExBlock) ladders1).setSubTypes(16);
        ((ExBlock) ladders2).setSubTypes(16);
        ((ExBlock) ladders3).setSubTypes(16);
        ((ExBlock) ladders4).setSubTypes(16);
        ((ExBlock) lights1).setSubTypes(14);
        ((ExBlock) plant2).setSubTypes(16);
        ((ExBlock) plant3).setSubTypes(16);
        ((ExBlock) overlay1).setSubTypes(7);
        ((ExBlock) stairs1).setSubTypes(4);
        ((ExBlock) stairs2).setSubTypes(4);
        ((ExBlock) stairs3).setSubTypes(4);
        ((ExBlock) stairs4).setSubTypes(4);
        ((ExBlock) slopes1).setSubTypes(4);
        ((ExBlock) slopes2).setSubTypes(4);
        ((ExBlock) slopes3).setSubTypes(4);
        ((ExBlock) slopes4).setSubTypes(4);

        Item.byId[pillarStone.id] = (new AC_ItemSubtypes(pillarStone.id - 256)).setTranslationKey("pillarStone");
        Item.byId[pillarMetal.id] = (new AC_ItemSubtypes(pillarMetal.id - 256)).setTranslationKey("pillarMetal");
        Item.byId[plant1.id] = (new AC_ItemSubtypes(plant1.id - 256)).setTranslationKey("flower");
        Item.byId[trees.id] = (new AC_ItemSubtypes(trees.id - 256)).setTranslationKey("sapling");
        Item.byId[glassBlocks.id] = (new AC_ItemSubtypes(glassBlocks.id - 256)).setTranslationKey("glass");
        Item.byId[cageBlocks.id] = (new AC_ItemSubtypes(cageBlocks.id - 256)).setTranslationKey("cage");
        Item.byId[stoneBlocks1.id] = (new AC_ItemSubtypes(stoneBlocks1.id - 256)).setTranslationKey("stone");
        Item.byId[stoneBlocks2.id] = (new AC_ItemSubtypes(stoneBlocks2.id - 256)).setTranslationKey("stone");
        Item.byId[stoneBlocks3.id] = (new AC_ItemSubtypes(stoneBlocks3.id - 256)).setTranslationKey("stone");
        Item.byId[woodBlocks.id] = (new AC_ItemSubtypes(woodBlocks.id - 256)).setTranslationKey("wood");
        Item.byId[halfSteps1.id] = (new AC_ItemSubtypes(halfSteps1.id - 256)).setTranslationKey("halfStep");
        Item.byId[halfSteps2.id] = (new AC_ItemSubtypes(halfSteps2.id - 256)).setTranslationKey("halfStep");
        Item.byId[halfSteps3.id] = (new AC_ItemSubtypes(halfSteps3.id - 256)).setTranslationKey("halfStep");
        Item.byId[tableBlocks.id] = (new AC_ItemSubtypes(tableBlocks.id - 256)).setTranslationKey("table");
        Item.byId[chairBlocks1.id] = (new AC_ItemSubtypes(chairBlocks1.id - 256)).setTranslationKey("chair");
        Item.byId[chairBlocks2.id] = (new AC_ItemSubtypes(chairBlocks2.id - 256)).setTranslationKey("chair");
        Item.byId[ropes1.id] = (new AC_ItemSubtypes(ropes1.id - 256)).setTranslationKey("rope");
        Item.byId[ropes2.id] = (new AC_ItemSubtypes(ropes2.id - 256)).setTranslationKey("rope");
        Item.byId[chains.id] = (new AC_ItemSubtypes(chains.id - 256)).setTranslationKey("chain");
        Item.byId[ladders1.id] = (new AC_ItemSubtypes(ladders1.id - 256)).setTranslationKey("ladder");
        Item.byId[ladders2.id] = (new AC_ItemSubtypes(ladders2.id - 256)).setTranslationKey("ladder");
        Item.byId[ladders3.id] = (new AC_ItemSubtypes(ladders3.id - 256)).setTranslationKey("ladder");
        Item.byId[ladders4.id] = (new AC_ItemSubtypes(ladders4.id - 256)).setTranslationKey("ladder");
        Item.byId[lights1.id] = (new AC_ItemSubtypes(lights1.id - 256)).setTranslationKey("torch");
        Item.byId[plant2.id] = (new AC_ItemSubtypes(plant2.id - 256)).setTranslationKey("flower");
        Item.byId[plant3.id] = (new AC_ItemSubtypes(plant3.id - 256)).setTranslationKey("flower");
        Item.byId[overlay1.id] = (new AC_ItemSubtypes(overlay1.id - 256)).setTranslationKey("overlay");
        Item.byId[stairs1.id] = (new AC_ItemSubtypes(stairs1.id - 256)).setTranslationKey("stairs");
        Item.byId[stairs2.id] = (new AC_ItemSubtypes(stairs2.id - 256)).setTranslationKey("stairs");
        Item.byId[stairs3.id] = (new AC_ItemSubtypes(stairs3.id - 256)).setTranslationKey("stairs");
        Item.byId[stairs4.id] = (new AC_ItemSubtypes(stairs4.id - 256)).setTranslationKey("stairs");
        Item.byId[slopes1.id] = (new AC_ItemSubtypes(slopes1.id - 256)).setTranslationKey("slopes");
        Item.byId[slopes2.id] = (new AC_ItemSubtypes(slopes2.id - 256)).setTranslationKey("slopes");
        Item.byId[slopes3.id] = (new AC_ItemSubtypes(slopes3.id - 256)).setTranslationKey("slopes");
        Item.byId[slopes4.id] = (new AC_ItemSubtypes(slopes4.id - 256)).setTranslationKey("slopes");

        for (int var0 = 0; var0 < 256; ++var0) {
            if (Block.BY_ID[var0] != null && Item.byId[var0] == null) {
                Item.byId[var0] = new BlockItem(var0 - 256);
                Block.BY_ID[var0].init();
            }
        }
    }
}
