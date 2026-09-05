package reallyfastmode.protocol.build;

/** Semantic Structure-of-Arrays snapshot for a group of monsters. */
public final class MonsterColumns {
    public final int size;
    public final int totalPowerEntries;

    public final byte[] instanceId4Bit;
    public final int[] monsterWireId;
    public final int[] hp;
    public final int[] maxHp;
    public final int[] block;

    public final int[] powerEntryCount;
    public final int[] powerWireId;
    public final int[] powerAmount;

    public final int[] intentWireId;
    public final int[] intentDamage;
    public final int[] intentMultiAmount;

    public MonsterColumns(int size) {
        this(size, 0);
    }

    public MonsterColumns(int size, int totalPowerEntries) {
        if (size < 0) {
            throw new IllegalArgumentException("size=" + size);
        }
        if (totalPowerEntries < 0) {
            throw new IllegalArgumentException("totalPowerEntries=" + totalPowerEntries);
        }

        this.size = size;
        this.totalPowerEntries = totalPowerEntries;
        this.instanceId4Bit = new byte[size];
        this.monsterWireId = new int[size];
        this.hp = new int[size];
        this.maxHp = new int[size];
        this.block = new int[size];
        this.powerEntryCount = new int[size];
        this.powerWireId = new int[totalPowerEntries];
        this.powerAmount = new int[totalPowerEntries];
        this.intentWireId = new int[size];
        this.intentDamage = new int[size];
        this.intentMultiAmount = new int[size];
    }
}
