package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.item.AC_ItemSubtypes;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TileItem;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class AC_Blocks {

    public static final Tile lockedDoor = (new AC_BlockLockedDoor(150, 208, AC_Items.doorKey.id)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("lockedDoor");
    public static final Tile lockedBossDoor = (new AC_BlockLockedDoor(156, 210, AC_Items.bossKey.id)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("lockedBossDoor");
    public static final Tile newMobSpawner = (new AC_BlockMobSpawner(151, 65)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("mobSpawner2");
    public static final Tile spawnBlock = (new AC_BlockSpawn(152, 0)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("spawn");
    public static final AC_BlockTrigger triggerBlock = (AC_BlockTrigger) (new AC_BlockTrigger(153, 1)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("trigger");
    public static final Tile triggerDoor = (new AC_BlockTriggeredDoor(154)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("triggeredDoor");
    public static final Tile spikeBlock = (new AC_BlockSpike(155)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("spike");
    public static final AC_BlockTriggerInverter triggerInverter = (AC_BlockTriggerInverter) (new AC_BlockTriggerInverter(157, 2)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("triggerInverter");
    public static final AC_BlockTriggerMemory triggerMemory = (AC_BlockTriggerMemory) (new AC_BlockTriggerMemory(158, 3)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("triggerMemory");
    public static final AC_BlockClip clipBlock = (AC_BlockClip) (new AC_BlockClip(159, 4, Material.AIR)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("clip");
    public static final AC_BlockRedstoneTrigger redstoneTrigger = (AC_BlockRedstoneTrigger) (new AC_BlockRedstoneTrigger(160, 228)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("redstoneTrigger");
    public static final AC_BlockRedstonePower redstonePower = (AC_BlockRedstonePower) (new AC_BlockRedstonePower(161, 185)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("redstonePower");
    public static final AC_BlockBombable bombableCobblestone = (AC_BlockBombable) (new AC_BlockBombable(162, 166, Material.STONE)).setDestroyTime(2.0F).setExplodeable(10.0F).setSoundType(Tile.SOUND_STONE).setDescriptionId("crackedStonebrick");
    public static final AC_BlockBombable bombableStone = (AC_BlockBombable) (new AC_BlockBombable(163, 167, Material.STONE)).setDestroyTime(1.5F).setExplodeable(10.0F).setSoundType(Tile.SOUND_STONE).setDescriptionId("crackedStone");
    public static final AC_BlockWeather weather = (AC_BlockWeather) (new AC_BlockWeather(164, 5)).setDestroyTime(1.5F).setExplodeable(10.0F).setSoundType(Tile.SOUND_STONE).setDescriptionId("weather");
    public static final AC_BlockMusic musicTriggered = (AC_BlockMusic) (new AC_BlockMusic(165, 9)).setDestroyTime(1.5F).setExplodeable(10.0F).setSoundType(Tile.SOUND_STONE).setDescriptionId("music");
    public static final Tile pushableBlock = (new AC_BlockPushable(166, 212, Material.STONE)).setDestroyTime(2.0F).setExplodeable(10.0F).setSoundType(Tile.SOUND_STONE).setDescriptionId("pushable");
    public static final AC_BlockTimer timer = (AC_BlockTimer) (new AC_BlockTimer(167, 8)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("timer");
    public static final AC_BlockMessage message = (AC_BlockMessage) (new AC_BlockMessage(168, 7)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("message");
    public static final Tile fan = (new AC_BlockFan(169, 184, true)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("fan");
    public static final Tile camera = (new AC_BlockCamera(170, 6)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("camera");
    public static final Tile lightBulb = (new AC_BlockLightBulb(171, 14)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("lightBulb");
    public static final Tile fanOff = (new AC_BlockFan(172, 200, false)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("fan");
    public static final Tile script = (new AC_BlockScript(173, 15)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("script");
    public static final Tile store = (new AC_BlockStore(174, 49)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_GLASS).setDescriptionId("store");
    public static final Tile effect = (new AC_BlockEffect(175, 244)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("effect");
    public static final Tile darkness = (new AC_BlockDarkness(200, 10)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("darkness");
    public static final Tile triggerPushable = (new AC_BlockTriggerPushable(201, 213)).setDestroyTime(2.0F).setExplodeable(10.0F).setSoundType(Tile.SOUND_STONE).setDescriptionId("triggerPushable");
    public static final Tile storage = (new AC_BlockStorage(202, 11)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("storage");
    public static final Tile healDamage = (new AC_BlockHealDamage(203, 12)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("healDamage");
    public static final Tile teleport = (new AC_BlockTeleport(204, 13)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("teleport");
    public static final Tile url = (new AC_BlockUrl(176, 245)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("url");
    public static final Tile npcPath = (new AC_BlockNpcPath(177, 247)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("npcPathBlock");
    public static final Tile pillarStone = (new AC_BlockPillar(205, 32)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_STONE).setDescriptionId("pillarStone");
    public static final Tile pillarMetal = (new AC_BlockPillar(206, 80)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("pillarMetal");
    public static final Tile plant1 = (new AC_BlockPlant(207, 112)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_GRASS).setDescriptionId("flower");
    public static final Tile trees = (new AC_BlockTree(208, 128)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_GRASS).setDescriptionId("sapling");
    public static final Tile glassBlocks = (new AC_BlockTransparent(209, 144)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_GLASS).setDescriptionId("glass");
    public static final Tile cageBlocks = (new AC_BlockTransparent(210, 160)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_GLASS).setDescriptionId("cage");
    public static final Tile stoneBlocks1 = (new AC_BlockSolid(211, 176)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_STONE).setDescriptionId("stone");
    public static final Tile stoneBlocks2 = (new AC_BlockSolid(212, 192)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_STONE).setDescriptionId("stone");
    public static final Tile stoneBlocks3 = (new AC_BlockSolid(213, 208)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_STONE).setDescriptionId("stone");
    public static final Tile woodBlocks = (new AC_BlockSolid(214, 224)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("wood");
    public static final Tile halfSteps1 = (new AC_BlockHalfStep(215, 240)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_STONE).setDescriptionId("halfStep");
    public static final Tile halfSteps2 = (new AC_BlockHalfStep(216, 0)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("halfStep");
    public static final Tile halfSteps3 = (new AC_BlockHalfStep(217, 16)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("halfStepWood");
    public static final Tile tableBlocks = (new AC_BlockTable(218, 32)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("table");
    public static final Tile chairBlocks1 = (new AC_BlockChair(219, 64)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("chair");
    public static final Tile chairBlocks2 = (new AC_BlockChair(220, 68)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("chair");
    public static final Tile chairBlocks3 = (new AC_BlockChair(221, 64)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("chair");
    public static final Tile chairBlocks4 = (new AC_BlockChair(222, 64)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("chair");
    public static final Tile ropes1 = (new AC_BlockRope(223, 96)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("rope");
    public static final Tile ropes2 = (new AC_BlockRope(224, 101)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("rope");
    public static final Tile chains = (new AC_BlockChain(225, 106)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("chain");
    public static final Tile ladders1 = (new AC_BlockLadderSubtypes(226, 112)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("ladder");
    public static final Tile ladders2 = (new AC_BlockLadderSubtypes(227, 116)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("ladder");
    public static final Tile ladders3 = (new AC_BlockLadderSubtypes(228, 120)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("ladder");
    public static final Tile ladders4 = (new AC_BlockLadderSubtypes(229, 124)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_WOOD).setDescriptionId("ladder");
    public static final Tile lights1 = (new AC_BlockPlant(230, 128)).setDestroyTime(5.0F).setLightEmission(15.0F / 16.0F).setSoundType(Tile.SOUND_METAL).setDescriptionId("torch");
    public static final Tile plant2 = (new AC_BlockTree(231, 144)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_GRASS).setDescriptionId("flower");
    public static final Tile plant3 = (new AC_BlockTree(232, 160)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_GRASS).setDescriptionId("flower");
    public static final Tile overlay1 = (new AC_BlockOverlay(233, 176)).setDestroyTime(5.0F).setSoundType(Tile.SOUND_GRASS).setDescriptionId("overlay");
    public static final Tile stairs1 = (new AC_BlockStairMulti(234, Tile.WOOD, 192)).setDescriptionId("stairs");
    public static final Tile stairs2 = (new AC_BlockStairMulti(235, Tile.WOOD, 196)).setDescriptionId("stairs");
    public static final Tile stairs3 = (new AC_BlockStairMulti(236, Tile.COBBLESTONE, 200)).setDescriptionId("stairs");
    public static final Tile stairs4 = (new AC_BlockStairMulti(237, Tile.COBBLESTONE, 204)).setDescriptionId("stairs");
    public static final Tile slopes1 = (new AC_BlockSlope(238, Tile.WOOD, 192)).setDescriptionId("slopes");
    public static final Tile slopes2 = (new AC_BlockSlope(239, Tile.WOOD, 196)).setDescriptionId("slopes");
    public static final Tile slopes3 = (new AC_BlockSlope(240, Tile.COBBLESTONE, 200)).setDescriptionId("slopes");
    public static final Tile slopes4 = (new AC_BlockSlope(241, Tile.COBBLESTONE, 204)).setDescriptionId("slopes");

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

        Item.items[pillarStone.id] = (new AC_ItemSubtypes(pillarStone.id - 256)).setDescriptionId("pillarStone");
        Item.items[pillarMetal.id] = (new AC_ItemSubtypes(pillarMetal.id - 256)).setDescriptionId("pillarMetal");
        Item.items[plant1.id] = (new AC_ItemSubtypes(plant1.id - 256)).setDescriptionId("flower");
        Item.items[trees.id] = (new AC_ItemSubtypes(trees.id - 256)).setDescriptionId("sapling");
        Item.items[glassBlocks.id] = (new AC_ItemSubtypes(glassBlocks.id - 256)).setDescriptionId("glass");
        Item.items[cageBlocks.id] = (new AC_ItemSubtypes(cageBlocks.id - 256)).setDescriptionId("cage");
        Item.items[stoneBlocks1.id] = (new AC_ItemSubtypes(stoneBlocks1.id - 256)).setDescriptionId("stone");
        Item.items[stoneBlocks2.id] = (new AC_ItemSubtypes(stoneBlocks2.id - 256)).setDescriptionId("stone");
        Item.items[stoneBlocks3.id] = (new AC_ItemSubtypes(stoneBlocks3.id - 256)).setDescriptionId("stone");
        Item.items[woodBlocks.id] = (new AC_ItemSubtypes(woodBlocks.id - 256)).setDescriptionId("wood");
        Item.items[halfSteps1.id] = (new AC_ItemSubtypes(halfSteps1.id - 256)).setDescriptionId("halfStep");
        Item.items[halfSteps2.id] = (new AC_ItemSubtypes(halfSteps2.id - 256)).setDescriptionId("halfStep");
        Item.items[halfSteps3.id] = (new AC_ItemSubtypes(halfSteps3.id - 256)).setDescriptionId("halfStep");
        Item.items[tableBlocks.id] = (new AC_ItemSubtypes(tableBlocks.id - 256)).setDescriptionId("table");
        Item.items[chairBlocks1.id] = (new AC_ItemSubtypes(chairBlocks1.id - 256)).setDescriptionId("chair");
        Item.items[chairBlocks2.id] = (new AC_ItemSubtypes(chairBlocks2.id - 256)).setDescriptionId("chair");
        Item.items[ropes1.id] = (new AC_ItemSubtypes(ropes1.id - 256)).setDescriptionId("rope");
        Item.items[ropes2.id] = (new AC_ItemSubtypes(ropes2.id - 256)).setDescriptionId("rope");
        Item.items[chains.id] = (new AC_ItemSubtypes(chains.id - 256)).setDescriptionId("chain");
        Item.items[ladders1.id] = (new AC_ItemSubtypes(ladders1.id - 256)).setDescriptionId("ladder");
        Item.items[ladders2.id] = (new AC_ItemSubtypes(ladders2.id - 256)).setDescriptionId("ladder");
        Item.items[ladders3.id] = (new AC_ItemSubtypes(ladders3.id - 256)).setDescriptionId("ladder");
        Item.items[ladders4.id] = (new AC_ItemSubtypes(ladders4.id - 256)).setDescriptionId("ladder");
        Item.items[lights1.id] = (new AC_ItemSubtypes(lights1.id - 256)).setDescriptionId("torch");
        Item.items[plant2.id] = (new AC_ItemSubtypes(plant2.id - 256)).setDescriptionId("flower");
        Item.items[plant3.id] = (new AC_ItemSubtypes(plant3.id - 256)).setDescriptionId("flower");
        Item.items[overlay1.id] = (new AC_ItemSubtypes(overlay1.id - 256)).setDescriptionId("overlay");
        Item.items[stairs1.id] = (new AC_ItemSubtypes(stairs1.id - 256)).setDescriptionId("stairs");
        Item.items[stairs2.id] = (new AC_ItemSubtypes(stairs2.id - 256)).setDescriptionId("stairs");
        Item.items[stairs3.id] = (new AC_ItemSubtypes(stairs3.id - 256)).setDescriptionId("stairs");
        Item.items[stairs4.id] = (new AC_ItemSubtypes(stairs4.id - 256)).setDescriptionId("stairs");
        Item.items[slopes1.id] = (new AC_ItemSubtypes(slopes1.id - 256)).setDescriptionId("slopes");
        Item.items[slopes2.id] = (new AC_ItemSubtypes(slopes2.id - 256)).setDescriptionId("slopes");
        Item.items[slopes3.id] = (new AC_ItemSubtypes(slopes3.id - 256)).setDescriptionId("slopes");
        Item.items[slopes4.id] = (new AC_ItemSubtypes(slopes4.id - 256)).setDescriptionId("slopes");

        for (int i = 0; i < 256; ++i) {
            if (Tile.tiles[i] != null && Item.items[i] == null) {
                Item.items[i] = new TileItem(i - 256);
                Tile.tiles[i].registerBurnables();
            }

            if (Tile.tiles[i] instanceof AC_BlockStairMulti) {
                ExBlock.neighborLit[i] = true;
            }
        }

        ExBlock.neighborLit[Tile.SLAB.id] = true;
        ExBlock.neighborLit[Tile.FARMLAND.id] = true;
        ExBlock.neighborLit[Tile.WOOD_STAIRS.id] = true;
        ExBlock.neighborLit[Tile.COBBLESTONE_STAIRS.id] = true;
    }
}
