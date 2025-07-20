package org.soujava.demos.mongodb.document;

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.databases.mongodb.mapping.MongoDBTemplate;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;


@EnableAutoWeld
@AddPackages(value = {Database.class, EntityConverter.class, DocumentTemplate.class, MongoDBTemplate.class})
@AddPackages(Room.class)
@AddPackages(ManagerSupplier.class)
@AddPackages(MongoDBTemplate.class)
@AddPackages(Reflections.class)
@AddPackages(Converters.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, DocumentExtension.class})
class RoomServiceTest {

    @Inject
    private RoomRepository repository;

    @BeforeEach
    void setUP() {

        Room vipRoom1 = new RoomBuilder()
                .roomNumber(101)
                .type(RoomType.VIP_SUITE)
                .status(RoomStatus.AVAILABLE)
                .cleanStatus(CleanStatus.CLEAN)
                .smokingAllowed(false)
                .build();

        Room vipRoom2 = new RoomBuilder()
                .roomNumber(102)
                .type(RoomType.VIP_SUITE)
                .status(RoomStatus.AVAILABLE)
                .cleanStatus(CleanStatus.CLEAN)
                .smokingAllowed(true)
                .build();

        Room standardRoom1 = new RoomBuilder()
                .roomNumber(201)
                .type(RoomType.STANDARD)
                .status(RoomStatus.AVAILABLE)
                .cleanStatus(CleanStatus.CLEAN)
                .smokingAllowed(false)
                .build();

        Room standardRoom2 = new RoomBuilder()
                .roomNumber(202)
                .type(RoomType.DELUXE)
                .status(RoomStatus.AVAILABLE)
                .cleanStatus(CleanStatus.CLEAN)
                .smokingAllowed(false)
                .build();

        Room dirtyReservedRoom = new RoomBuilder()
                .roomNumber(301)
                .type(RoomType.DELUXE)
                .status(RoomStatus.RESERVED)
                .cleanStatus(CleanStatus.DIRTY)
                .smokingAllowed(false)
                .build();

        Room dirtySuiteRoom = new RoomBuilder()
                .roomNumber(302)
                .type(RoomType.SUITE)
                .status(RoomStatus.UNDER_MAINTENANCE)
                .cleanStatus(CleanStatus.INSPECTION_NEEDED)
                .smokingAllowed(false)
                .build();

        Room smokingAllowedRoom = new RoomBuilder()
                .roomNumber(401)
                .type(RoomType.STANDARD)
                .status(RoomStatus.AVAILABLE)
                .cleanStatus(CleanStatus.CLEAN)
                .smokingAllowed(true)
                .build();

        repository.save(List.of(
                vipRoom1, vipRoom2,
                standardRoom1, standardRoom2,
                dirtyReservedRoom, dirtySuiteRoom,
                smokingAllowedRoom
        ));

    }

    @AfterEach
    void cleanUp() {
        repository.deleteBy();
    }

    @Test
    void shouldFindRoomReadyToGuest() {
        List<Room> rooms = this.repository.findAvailableStandardRooms();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(rooms).hasSize(3);
            softly.assertThat(rooms).allMatch(room -> room.getStatus().equals(RoomStatus.AVAILABLE));
            softly.assertThat(rooms).allMatch(room -> !room.isUnderMaintenance());
        });
    }

    @Test
    void shouldFindVipRoomsReadyForGuests() {
        List<Room> rooms = this.repository.findVipRoomsReadyForGuests();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(rooms).hasSize(2);
            softly.assertThat(rooms).allMatch(room -> room.getType().equals(RoomType.VIP_SUITE));
            softly.assertThat(rooms).allMatch(room -> room.getStatus().equals(RoomStatus.AVAILABLE));
            softly.assertThat(rooms).allMatch(room -> !room.isUnderMaintenance());
        });
    }

    @Test
    void shouldFindAvailableSmokingRooms() {
        List<Room> rooms = this.repository.findAvailableSmokingRooms();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(rooms).hasSize(2);
            softly.assertThat(rooms).allMatch(room -> room.isSmokingAllowed());
            softly.assertThat(rooms).allMatch(room -> room.getStatus().equals(RoomStatus.AVAILABLE));
        });
    }

    @Test
    void shouldFindRoomsNeedingCleaning() {
        List<Room> rooms = this.repository.findRoomsNeedingCleaning();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(rooms).hasSize(2);
            softly.assertThat(rooms).allMatch(room -> !room.getCleanStatus().equals(CleanStatus.CLEAN));
            softly.assertThat(rooms).allMatch(room -> !room.getStatus().equals(RoomStatus.OUT_OF_SERVICE));
        });
    }

    @ParameterizedTest(name = "should find rooms by type {0}")
    @EnumSource(RoomType.class)
    void shouldFindRoomByType(RoomType type) {
        List<Room> rooms = this.repository.findByType(type);
        SoftAssertions.assertSoftly(softly -> softly.assertThat(rooms).allMatch(room -> room.getType().equals(type)));
    }

    @ParameterizedTest
    @MethodSource("room")
    void shouldSaveRoom(Room room) {
        Room updateRoom = this.repository.newRoom(room);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updateRoom).isNotNull();
            softly.assertThat(updateRoom.getId()).isNotNull();
            softly.assertThat(updateRoom.getRoomNumber()).isEqualTo(room.getRoomNumber());
            softly.assertThat(updateRoom.getType()).isEqualTo(room.getType());
            softly.assertThat(updateRoom.getStatus()).isEqualTo(room.getStatus());
            softly.assertThat(updateRoom.getCleanStatus()).isEqualTo(room.getCleanStatus());
            softly.assertThat(updateRoom.isSmokingAllowed()).isEqualTo(room.isSmokingAllowed());
        });
    }

    static Stream<Arguments> room() {
        Room room = new RoomBuilder()
                .roomNumber(101)
                .type(RoomType.VIP_SUITE)
                .status(RoomStatus.AVAILABLE)
                .cleanStatus(CleanStatus.CLEAN)
                .smokingAllowed(false)
                .build();

        return Stream.of(Arguments.of(room));
    }
}