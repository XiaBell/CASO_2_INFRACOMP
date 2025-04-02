public class Testpad{
    public static void main(String[] args) {
        PageManager manager = new PageManager(10, 3); // 10 pages, 3 frames
	manager.accessPage(0, false); // Miss
	manager.accessPage(1, false); // Miss
	manager.accessPage(0, true);  // Hit
	manager.accessPage(2, false); // Miss
	manager.accessPage(1, true);  // Hit

	int totalAccesses = 5;
	assert manager.validateHitMissConsistency(totalAccesses);
    }
}