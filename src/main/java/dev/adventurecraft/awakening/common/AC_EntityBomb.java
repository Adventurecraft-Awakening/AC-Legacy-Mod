package dev.adventurecraft.awakening.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

public class AC_EntityBomb extends ItemEntity {
    private static final double BOMB_DAMAGE = 20.0D;
    private static final double BOMB_RANGE = 5.0D;
    private static final double BOMB_DESTROY_RANGE = 3.0D;
    private static final int BOMB_FUSE = 45;
    private int fuse;
    private Entity parentEntity;

    public AC_EntityBomb(Level var1) {
        super(var1);
        this.setSize(0.5F, 0.5F);
        this.item = new ItemInstance(AC_Items.bomb);
        this.fuse = BOMB_FUSE;
    }

    public AC_EntityBomb(Level world, Entity entity) {
        this(world);
        this.parentEntity = entity;
        this.setRot(entity.yRot, entity.xRot);
        this.xd = 0.3D * -Math.sin(this.yRot / 180.0F * 3.141593F) * Math.cos(this.xRot / 180.0F * 3.141593F);
        this.zd = 0.3D * Math.cos(this.yRot / 180.0F * 3.141593F) * Math.cos(this.xRot / 180.0F * 3.141593F);
        this.yd = 0.3D * -Math.sin(this.xRot / 180.0F * 3.141593F) + (double) 0.1F;
        this.setPos(entity.x, entity.y, entity.z);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    public void tick() {
        super.tick();
        if (this.fuse == 45) {
            this.level.playSound(this, "random.fuse", 1.0F, 1.0F);
        }

        --this.fuse;
        double fuseRemaining = (double) this.fuse / BOMB_FUSE;
        // The particle effects animate as if the fuse was burning, drawing the fire and smoke lower!
        double flameSourceMod = 0.2D * fuseRemaining;

        if (this.fuse == 0) {
            explode(this, this.parentEntity, this.level, this.x, this.y, this.z);
        } else if (this.fuse % 2 == 0) {
            this.level.addParticle("smoke", this.x, this.y + 0.675D + flameSourceMod, this.z, 0.0D, 0.0D, 0.0D);
        } else {
            this.level.addParticle("flame", this.x, this.y + 0.675D + flameSourceMod, this.z, 0.0D, 0.0D, 0.0D);
        }

    }

    /**
     * Deals damage around a given entity (radius defined by the BOMB_RANGE)
     * @param world The world where the entities will be harmed
     * @param exploder The entity that will explode
     * @param explosionParent The owner of the explosive (the owner of the damage)
     */
    private static void harmEntitiesAround(Level world, Entity exploder, Entity explosionParent) {
        double x = exploder.x;
        double y = exploder.y;
        double z = exploder.z;
        var victims = world.getEntities(
            exploder,
            AABB.newTemp(
                Math.floor(x - BOMB_RANGE),
                Math.floor(y - BOMB_RANGE),
                Math.floor(z - BOMB_RANGE),
                Math.ceil(x + BOMB_RANGE),
                Math.ceil(y + BOMB_RANGE),
                Math.ceil(z + BOMB_RANGE)
            )
        );
        List<Entity> appliedForceOnEntity = new LinkedList<>();
        for (int i = 0; i < victims.size(); i++) {
            if(i >= victims.size()){
                break;
            }
            Entity victim = (Entity) victims.get(i);
            if (!victim.isAlive()) {
                continue;
            }
            if(appliedForceOnEntity.contains(victim)){
                continue;
            }

            double distanceFromExplosion = victim.distanceTo(x, y, z);
            if (distanceFromExplosion < BOMB_RANGE) {
                distanceFromExplosion = (BOMB_RANGE - distanceFromExplosion) / BOMB_RANGE; // Percentage of how close the character is
                double xForce = victim.x - x;
                double yForce = victim.y - y;
                double zForce = victim.z - z;
                victim.push(distanceFromExplosion * xForce, distanceFromExplosion * yForce, distanceFromExplosion * zForce);
                victim.hurt(explosionParent, (int) Math.ceil(distanceFromExplosion * BOMB_DAMAGE));
                appliedForceOnEntity.add(victim);
            }
        }
        appliedForceOnEntity.clear();
    }

    /**
     * Destroys blocks around a given block (radius defined by BOMB_DESTROY_RANGE)
     * @param world The world to check for bombable blocks
     * @param x The source of the explosion's X position.
     * @param y The source of the explosion's Y position.
     * @param z The source of the explosion's Z position.
     */
    private static void destroyBombableBlocksAround(Level world, int x, int y, int z) {

        int bombDestroyRange = (int) BOMB_DESTROY_RANGE;
        // Look for blocks in a volume centered on the explosion's center block's origin corner.
        for (int blockOffsetX = -bombDestroyRange; blockOffsetX <= bombDestroyRange; ++blockOffsetX) {
            for (int blockOffsetY = -bombDestroyRange; blockOffsetY <= bombDestroyRange; ++blockOffsetY) {
                for (int blockOffsetZ = -bombDestroyRange; blockOffsetZ <= bombDestroyRange; ++blockOffsetZ) {
                    double distanceSquared = (double) blockOffsetX * (double) blockOffsetX + (double) (blockOffsetY * blockOffsetY) + (double) (blockOffsetZ * blockOffsetZ);
                    if (distanceSquared <= 9.0D) {
                        int blockAtOffset = world.getTile(x + blockOffsetX, y + blockOffsetY, z + blockOffsetZ);
                        // Remove bombable tiles
                        if (Tile.tiles[blockAtOffset] instanceof AC_BlockBombable) {
                            world.setTile(x + blockOffsetX, y + blockOffsetY, z + blockOffsetZ, 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * Displays explosion particles sourced from a position.
     * @param world The world in which to spawn the particles.
     * @param x The source of the particles' X position.
     * @param y The source of the particles' Y position.
     * @param z The source of the particles' Z position.
     */
    private static void displayExplosionParticles(Level world, double x, double y, double z) {
        Random rng = new Random();
        rng.setSeed(world.getTime());

        // This is for the smoke coming out of the explosion.
        for (int xDirectionForce = -3; xDirectionForce <= 3; ++xDirectionForce) { // Used for X
            for (int yDirectionForce = -3; yDirectionForce <= 3; ++yDirectionForce) { // Used for Y
                for (int zDirectionForce = -3; zDirectionForce <= 3; ++zDirectionForce) {

                    double distanceSquared = (double) xDirectionForce * (double) xDirectionForce + (double) (yDirectionForce * yDirectionForce) + (double) (zDirectionForce * zDirectionForce);
                    if (rng.nextInt(3) == 0 && distanceSquared <= 9.0D) {
                        double launchPower = Math.sqrt(distanceSquared) * (0.375D + 0.25D * rng.nextDouble());
                        double xDirection = xDirectionForce / launchPower;
                        double yDirection = yDirectionForce / launchPower;
                        double zDirection = zDirectionForce / launchPower;

                        world.addParticle("explode", x, y, z, xDirection, yDirection, zDirection);
                        world.addParticle("smoke", x, y, z, xDirection, yDirection, zDirection);
                    }
                }
            }
        }
    }

    public static void explode(Entity exploder, Entity parent, Level world, double x, double y, double z) {
        exploder.remove();
        world.playSound(x, y, z, "random.explode", 4.0F, 1.0F);

        harmEntitiesAround(world, exploder, parent);

        destroyBombableBlocksAround(world, (int) x, (int) y, (int) z);

        displayExplosionParticles(world, x, y, z);

    }

    public boolean hurt(Entity var1, int var2) {
        if (!this.removed) {
            this.markHurt();
            explode(this, this.parentEntity, this.level, this.x, this.y, this.z);
        }

        return false;
    }

    public void readAdditionalSaveData(CompoundTag var1) {
        super.readAdditionalSaveData(var1);
        var1.putByte("Fuse", (byte) this.fuse);
    }

    public void addAdditionalSaveData(CompoundTag var1) {
        super.addAdditionalSaveData(var1);
        this.fuse = var1.getByte("Fuse");
    }

    public void playerTouch(Player var1) {
    }
}
