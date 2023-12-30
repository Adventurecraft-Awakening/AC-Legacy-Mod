package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class AC_EntityBomb extends ItemEntity {
    private static final double BOMB_DAMAGE = 20.0D;
    private static final double BOMB_RANGE = 5.0D;
    private static final double BOMB_DESTROY_RANGE = 3.0D;
    private static final int BOMB_FUSE = 45;
    private int fuse;
    private Entity parentEntity;

    public AC_EntityBomb(World var1) {
        super(var1);
        this.setSize(0.5F, 0.5F);
        this.stack = new ItemStack(AC_Items.bomb);
        this.fuse = BOMB_FUSE;
    }

    public AC_EntityBomb(World world, Entity entity) {
        this(world);
        this.parentEntity = entity;
        this.setRotation(entity.yaw, entity.pitch);
        this.xVelocity = 0.3D * -Math.sin(this.yaw / 180.0F * 3.141593F) * Math.cos(this.pitch / 180.0F * 3.141593F);
        this.zVelocity = 0.3D * Math.cos(this.yaw / 180.0F * 3.141593F) * Math.cos(this.pitch / 180.0F * 3.141593F);
        this.yVelocity = 0.3D * -Math.sin(this.pitch / 180.0F * 3.141593F) + (double) 0.1F;
        this.setPosition(entity.x, entity.y, entity.z);
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
    }

    public void tick() {
        super.tick();
        if (this.fuse == 45) {
            this.world.playSound(this, "random.fuse", 1.0F, 1.0F);
        }

        --this.fuse;
        double fuseRemaining = (double) this.fuse / BOMB_FUSE;
        // The particle effects animate as if the fuse was burning, drawing the fire and smoke lower!
        double flameSourceMod = 0.2D * fuseRemaining;

        if (this.fuse == 0) {
            explode(this, this.parentEntity, this.world, this.x, this.y, this.z);
        } else if (this.fuse % 2 == 0) {
            this.world.addParticle("smoke", this.x, this.y + 0.675D + flameSourceMod, this.z, 0.0D, 0.0D, 0.0D);
        } else {
            this.world.addParticle("flame", this.x, this.y + 0.675D + flameSourceMod, this.z, 0.0D, 0.0D, 0.0D);
        }

    }

    /**
     * Deals damage around a given entity (Defined by the BOMB_RANGE)
     * @param world The world where the entities will be harmed
     * @param exploder The entity that will explode
     * @param explosionParent The owner of the explosive (the owner of the damage)
     */
    private static void harmEntitiesAround(World world, Entity exploder, Entity explosionParent) {
        double x = exploder.x;
        double y = exploder.y;
        double z = exploder.z;
        var victims = world.getEntities(
            exploder,
            AxixAlignedBoundingBox.createAndAddToList(
                Math.floor(x - BOMB_RANGE),
                Math.floor(y - BOMB_RANGE),
                Math.floor(z - BOMB_RANGE),
                Math.ceil(x + BOMB_RANGE),
                Math.ceil(y + BOMB_RANGE),
                Math.ceil(z + BOMB_RANGE)
            )
        );

        for (Object o : victims) {
            Entity victim = (Entity) o;
            // TODO: This is a band-aid fix that prevents players' accumulated hit boxes from being accelerated (and thus accelerating multiple times from a single explosion.)
            if (!victim.isAlive()) continue;
            // End of band-aid fix
            double distanceFromExplosion = victim.distanceTo(x, y, z);
            if (distanceFromExplosion < BOMB_RANGE) {
                distanceFromExplosion = (BOMB_RANGE - distanceFromExplosion) / BOMB_RANGE; // Percentage of how close the character is
                double xForce = victim.x - x;
                double yForce = victim.y - y;
                double zForce = victim.z - z;
                victim.accelerate(distanceFromExplosion * xForce, distanceFromExplosion * yForce, distanceFromExplosion * zForce);
                victim.damage(explosionParent, (int) Math.ceil(distanceFromExplosion * BOMB_DAMAGE));
            }
        }
    }

    private static void destroyBombableBlocksAround(World world, int x, int y, int z) {

        int bombDestroyRange = (int) BOMB_DESTROY_RANGE;
        // Look for blocks in a volume centered on the explosion's center block's origin corner.
        for (int blockOffsetX = -bombDestroyRange; blockOffsetX <= bombDestroyRange; ++blockOffsetX) {
            for (int blockOffsetY = -bombDestroyRange; blockOffsetY <= bombDestroyRange; ++blockOffsetY) {
                for (int blockOffsetZ = -bombDestroyRange; blockOffsetZ <= bombDestroyRange; ++blockOffsetZ) {
                    double distanceSquared = (double) blockOffsetX * (double) blockOffsetX + (double) (blockOffsetY * blockOffsetY) + (double) (blockOffsetZ * blockOffsetZ);
                    if (distanceSquared <= 9.0D) {
                        int blockAtOffset = world.getBlockId(x + blockOffsetX, y + blockOffsetY, z + blockOffsetZ);
                        // Remove bombable tiles
                        if (Block.BY_ID[blockAtOffset] instanceof AC_BlockBombable) {
                            world.setBlock(x + blockOffsetX, y + blockOffsetY, z + blockOffsetZ, 0);
                        }
                    }
                }
            }
        }
    }

    private static void displayExplosionParticles(World world, double x, double y, double z) {
        Random rng = new Random();
        rng.setSeed(world.getWorldTime());

        // This is for the smoke coming out of the explosion.
        for (int xDirectionForce = -3; xDirectionForce <= 3; ++xDirectionForce) { // Used for X
            for (int yDirectionForce = -3; yDirectionForce <= 3; ++yDirectionForce) { // Used for Y
                for (int zDirectionForce = -3; zDirectionForce <= 3; ++zDirectionForce) {
                    double distanceSquared = (double) xDirectionForce * (double) xDirectionForce + (double) (yDirectionForce * yDirectionForce) + (double) (zDirectionForce * zDirectionForce);
                    if (rng.nextInt(3) == 0 && distanceSquared <= 9.0D) {
                        double xDirection = xDirectionForce;
                        double yDirection = yDirectionForce;
                        double zDirection = zDirectionForce;
                        double launchPower = Math.sqrt(distanceSquared) * (0.75D + 0.5D * rng.nextDouble()) * 1.5D / 3.0D;
                        xDirection = xDirection / launchPower;
                        yDirection = yDirection / launchPower;
                        zDirection = zDirection / launchPower;
                        world.addParticle("explode", x, y, z, xDirection, yDirection, zDirection);
                        world.addParticle("smoke", x, y, z, xDirection, yDirection, zDirection);
                    }
                }
            }
        }
    }

    public static void explode(Entity exploder, Entity parent, World world, double x, double y, double z) {
        exploder.remove();
        world.playSound(x, y, z, "random.explode", 4.0F, 1.0F);

        harmEntitiesAround(world, exploder, parent);

        destroyBombableBlocksAround(world, (int) x, (int) y, (int) z);

        displayExplosionParticles(world, x, y, z);

    }

    public boolean damage(Entity var1, int var2) {
        if (!this.removed) {
            this.setAttacked();
            explode(this, this.parentEntity, this.world, this.x, this.y, this.z);
        }

        return false;
    }

    public void writeAdditional(CompoundTag var1) {
        super.writeAdditional(var1);
        var1.put("Fuse", (byte) this.fuse);
    }

    public void readAdditional(CompoundTag var1) {
        super.readAdditional(var1);
        this.fuse = var1.getByte("Fuse");
    }

    public void onPlayerCollision(PlayerEntity var1) {
    }
}
