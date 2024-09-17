package dev.adventurecraft.awakening.common;

import net.minecraft.world.item.Item;

public class AC_Items {
    public static Item boomerang = (new AC_ItemBoomerang(200)).setDescriptionId("boomerang").handEquipped();
    public static Item doorKey = (new Item(201)).texture(145).setDescriptionId("key");
    public static AC_ItemHookshot hookshot = (AC_ItemHookshot) (new AC_ItemHookshot(202)).setDescriptionId("hookshot").handEquipped();
    public static Item heart = (new Item(203)).texture(146).setDescriptionId("heart");
    public static Item heartContainer = (new Item(204)).texture(147).setDescriptionId("heartContainer");
    public static Item woodenShield = (new Item(205)).texture(148).setDescriptionId("woodenShield");
    public static Item bossKey = (new Item(209)).texture(149).setDescriptionId("bossKey");
    public static Item bomb = (new AC_ItemBomb(210)).setDescriptionId("bomb").handEquipped();
    public static Item bombArow = (new Item(211)).texture(166).setDescriptionId("bombArrow");
    public static Item powerGlove = (new AC_ItemPowerGlove(212)).texture(177).setDescriptionId("powerGlove");
    public static Item heartPiece = (new Item(213)).texture(176).setDescriptionId("heartPiece");
    public static Item umbrella = (new AC_ItemUmbrella(214)).texture(179).setDescriptionId("umbrella").handEquipped();
    public static Item lantern = (new AC_ItemLantern(215)).texture(180).setDescriptionId("lantern").setMaxDamage(1200).stacksTo(1);
    public static Item oil = (new Item(216)).texture(181).setDescriptionId("oil");
    public static Item pistol = (new AC_ItemPistol(217)).texture(192).setDescriptionId("pistol").setMaxDamage(15).handEquipped();
    public static Item rifle = (new AC_ItemRifle(218)).texture(193).setDescriptionId("rifle").setMaxDamage(30).handEquipped();
    public static Item shotgun = (new AC_ItemShotgun(219)).texture(194).setDescriptionId("shotgun").setMaxDamage(7).handEquipped();
    public static Item pistolAmmo = (new Item(230)).texture(208).setDescriptionId("pistolAmmo");
    public static Item rifleAmmo = (new Item(231)).texture(209).setDescriptionId("rifleAmmo");
    public static Item shotgunAmmo = (new Item(232)).texture(210).setDescriptionId("shotgunAmmo");
    public static Item harp = (new AC_ItemInstrument(206, "note.harp")).texture(160).setDescriptionId("harp").handEquipped();
    public static Item guitar = (new AC_ItemInstrument(207, "note.bass")).texture(161).setDescriptionId("guitar").handEquipped();
    public static Item snare = (new AC_ItemInstrument(208, "note.snare")).texture(162).setDescriptionId("snare").handEquipped();
    public static Item pegagusBoots = (new AC_ItemPegasusBoots(240)).setDescriptionId("pegasusBoots");
    public static Item cursor = (new AC_ItemCursor(300)).texture(224).setDescriptionId("cursor").handEquipped();
    public static Item brush = (new AC_ItemBrush(301)).texture(225).setDescriptionId("brush").handEquipped();
    public static Item eraser = (new AC_ItemEraser(302)).texture(226).setDescriptionId("eraser").handEquipped();
    public static Item paintBucket = (new AC_ItemPaintBucket(303)).texture(227).setDescriptionId("paintBucket");
    public static Item hammer = (new AC_ItemHammer(304)).texture(228).setDescriptionId("hammer").handEquipped();
    public static Item wrench = (new AC_ItemWrench(305)).texture(230).setDescriptionId("wrench").handEquipped();
    public static Item npcStick = (new AC_ItemNPCStick(306)).setDescriptionId("npcStick").handEquipped();
    public static Item triggerStick = (new AC_ItemTriggerStick(307)).setDescriptionId("triggerStick").handEquipped();
    public static Item quill = (new AC_ItemQuill(330)).texture(229).setDescriptionId("quill");
    public static Item paste = (new AC_ItemPaste(308)).texture(231).setDescriptionId("paste");
    public static Item nudge = (new AC_ItemNudge(309)).texture(232).setDescriptionId("nudge");
}
