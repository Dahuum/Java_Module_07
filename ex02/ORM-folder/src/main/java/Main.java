import orm.OrmManager;
import models.User;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Complete ORM Test ===\n");
        
        // Create ORM Manager (this will create tables)
        OrmManager orm = new OrmManager();
        
        System.out.println("=== Testing SAVE ===");
        
        // Create and save users
        User user1 = new User("John", "Doe", 30);
        orm.save(user1);
        
        User user2 = new User("Jane", "Smith", 25);
        orm.save(user2);
        
        User user3 = new User("Bob", "Johnson", 35);
        orm.save(user3);
        
        System.out.println("=== Testing FIND BY ID ===");
        
        // Find users by ID
        User foundUser1 = orm.findById(1L, User.class);
        User foundUser2 = orm.findById(2L, User.class);
        User notFound = orm.findById(999L, User.class);
        
        System.out.println("=== Testing UPDATE ===");
        
        // Update a user
        if (foundUser1 != null) {
            foundUser1.setAge(31);
            foundUser1.setFirstName("Johnny");
            orm.update(foundUser1);
        }
        
        // Verify the update
        User updatedUser = orm.findById(1L, User.class);
        
        System.out.println("=== Final Cleanup ===");
        
        // Clean up (this will drop tables)
        orm.dropTables();
        
        System.out.println("🎉 All tests complete!");
    }
}