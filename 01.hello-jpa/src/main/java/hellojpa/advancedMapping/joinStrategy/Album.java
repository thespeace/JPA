package hellojpa.advancedMapping.joinStrategy;

import jakarta.persistence.Entity;

@Entity
public class Album extends Item {

    private String artist;
}
