package dev.adventurecraft.awakening.common;

import net.minecraft.item.Item;

public class AC_Items {
	public static Item boomerang = (new AC_ItemBoomerang(200)).setTranslationKey("boomerang").setRendered3d();
	public static Item doorKey = (new Item(201)).setTexturePosition(145).setTranslationKey("key");
	public static AC_ItemHookshot hookshot = (AC_ItemHookshot)(new AC_ItemHookshot(202)).setTranslationKey("hookshot").setRendered3d();
	public static Item heart = (new Item(203)).setTexturePosition(146).setTranslationKey("heart");
	public static Item heartContainer = (new Item(204)).setTexturePosition(147).setTranslationKey("heartContainer");
	public static Item woodenShield = (new Item(205)).setTexturePosition(148).setTranslationKey("woodenShield");
	public static Item bossKey = (new Item(209)).setTexturePosition(149).setTranslationKey("bossKey");
	public static Item bomb = (new AC_ItemBomb(210)).setTranslationKey("bomb").setRendered3d();
	public static Item bombArow = (new Item(211)).setTexturePosition(166).setTranslationKey("bombArrow");
	public static Item powerGlove = (new AC_ItemPowerGlove(212)).setTexturePosition(177).setTranslationKey("powerGlove");
	public static Item heartPiece = (new Item(213)).setTexturePosition(176).setTranslationKey("heartPiece");
	public static Item umbrella = (new AC_ItemUmbrella(214)).setTexturePosition(179).setTranslationKey("umbrella").setRendered3d();
	public static Item lantern = (new AC_ItemLantern(215)).setTexturePosition(180).setTranslationKey("lantern").setDurability(1200).setMaxStackSize(1);
	public static Item oil = (new Item(216)).setTexturePosition(181).setTranslationKey("oil");
	//public static Item pistol = (new AC_ItemPistol(217)).setTexturePosition(192).setTranslationKey("pistol").setDurability(15).setRendered3d(); TODO
	//public static Item rifle = (new AC_ItemRifle(218)).setTexturePosition(193).setTranslationKey("rifle").setDurability(30).setRendered3d(); TODO
	//public static Item shotgun = (new AC_ItemShotgun(219)).setTexturePosition(194).setTranslationKey("shotgun").setDurability(7).setRendered3d(); TODO
	public static Item pistolAmmo = (new Item(230)).setTexturePosition(208).setTranslationKey("pistolAmmo");
	public static Item rifleAmmo = (new Item(231)).setTexturePosition(209).setTranslationKey("rifleAmmo");
	public static Item shotgunAmmo = (new Item(232)).setTexturePosition(210).setTranslationKey("shotgunAmmo");
	public static Item harp = (new AC_ItemInstrument(206, "note.harp")).setTexturePosition(160).setTranslationKey("harp").setRendered3d();
	public static Item guitar = (new AC_ItemInstrument(207, "note.bass")).setTexturePosition(161).setTranslationKey("guitar").setRendered3d();
	public static Item snare = (new AC_ItemInstrument(208, "note.snare")).setTexturePosition(162).setTranslationKey("snare").setRendered3d();
	public static Item pegagusBoots = (new AC_ItemPegasusBoots(240)).setTranslationKey("pegasusBoots");
	public static Item cursor = (new AC_ItemCursor(300)).setTexturePosition(224).setTranslationKey("cursor").setRendered3d();
	public static Item brush = (new AC_ItemBrush(301)).setTexturePosition(225).setTranslationKey("brush").setRendered3d();
	public static Item eraser = (new AC_ItemEraser(302)).setTexturePosition(226).setTranslationKey("eraser").setRendered3d();
	public static Item paintBucket = (new AC_ItemPaintBucket(303)).setTexturePosition(227).setTranslationKey("paintBucket");
	public static Item hammer = (new AC_ItemHammer(304)).setTexturePosition(228).setTranslationKey("hammer").setRendered3d();
	public static Item wrench = (new AC_ItemWrench(305)).setTexturePosition(230).setTranslationKey("wrench").setRendered3d();
	//public static Item npcStick = (new AC_ItemNPCStick(306)).setTranslationKey("npcStick").setRendered3d(); TODO
	public static Item triggerStick = (new AC_ItemTriggerStick(307)).setTranslationKey("triggerStick").setRendered3d();
	public static Item quill = (new AC_ItemQuill(330)).setTexturePosition(229).setTranslationKey("quill");
	public static Item paste = (new AC_ItemPaste(308)).setTexturePosition(231).setTranslationKey("paste");
	public static Item nudge = (new AC_ItemNudge(309)).setTexturePosition(232).setTranslationKey("nudge");
}
