package org.soujava.demos.mongodb.document;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

@Entity
public class Room {

    @Id
    private String id;

    @Column
    private int roomNumber;

    @Column
    private RoomType type;

    @Column
    private RoomStatus status;

    @Column
    private CleanStatus cleanStatus;

    @Column
    private boolean smokingAllowed;

    @Column
    private boolean underMaintenance;

}