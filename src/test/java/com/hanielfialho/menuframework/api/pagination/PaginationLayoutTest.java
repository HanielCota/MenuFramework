package com.hanielfialho.menuframework.api.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.layout.SlotPatterns;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class PaginationLayoutTest {

  @Test
  void contentAreaPreservesRowMajorOrder() {
    MenuLayout menuLayout = MenuLayout.chest(4);

    PaginationLayout pagination =
        PaginationLayout.builder(menuLayout)
            .contentArea(1, 1, 2, 3)
            .previousSlot(3, 0)
            .indicatorSlot(3, 4)
            .nextSlot(3, 8)
            .build();

    assertEquals(List.of(10, 11, 12, 19, 20, 21), pagination.contentSlots());
    assertEquals(6, pagination.pageSize());
    assertEquals(27, pagination.previousSlot());
    assertEquals(31, pagination.indicatorSlot().orElseThrow());
    assertEquals(35, pagination.nextSlot());
  }

  @Test
  void contentRegionAndControlsCanUseLayoutNames() {
    MenuLayout menuLayout =
        MenuLayout.chestBuilder(4)
            .region("entries", SlotPatterns.rectangle(1, 1, 2, 3))
            .slot("previous", 3, 0)
            .slot("indicator", 3, 4)
            .slot("next", 3, 8)
            .build();

    PaginationLayout pagination =
        PaginationLayout.builder(menuLayout)
            .contentRegion("entries")
            .previousSlot("previous")
            .indicatorSlot("indicator")
            .nextSlot("next")
            .build();

    assertEquals(List.of(10, 11, 12, 19, 20, 21), pagination.contentSlots());
    assertEquals(27, pagination.previousSlot());
    assertEquals(31, pagination.indicatorSlot().orElseThrow());
    assertEquals(35, pagination.nextSlot());
  }

  @Test
  void mapsEntriesToSlotsAndAbsoluteIndexes() {
    MenuLayout menuLayout = MenuLayout.chest(2);
    PaginationLayout pagination =
        PaginationLayout.builder(menuLayout)
            .contentSlots(0, 2, 4)
            .previousSlot(15)
            .nextSlot(17)
            .build();

    PageSlice<String> page =
        PageSlice.unknownTotal(new PageRequest(PageCursor.of(2), 3), List.of("a", "b"), false);

    List<String> mappings = new ArrayList<>();

    pagination.forEachEntry(
        page,
        (slot, entry, indexInPage, absoluteIndex) ->
            mappings.add(slot + ":" + entry + ":" + indexInPage + ":" + absoluteIndex));

    assertEquals(List.of("0:a:0:6", "2:b:1:7"), mappings);
  }

  @Test
  void exposesOnlyUnusedContentSlots() {
    MenuLayout menuLayout = MenuLayout.chest(2);
    PaginationLayout pagination =
        PaginationLayout.builder(menuLayout)
            .contentSlots(0, 2, 4)
            .previousSlot(15)
            .nextSlot(17)
            .build();

    PageSlice<String> page = PageSlice.unknownTotal(PageRequest.first(3), List.of("a"), false);

    List<Integer> unused = new ArrayList<>();
    pagination.forEachUnusedSlot(page, unused::add);

    assertEquals(List.of(2, 4), unused);
  }

  @Test
  void rejectsDuplicateAndOverlappingSlots() {
    MenuLayout layout = MenuLayout.chest(2);

    assertThrows(
        IllegalArgumentException.class, () -> PaginationLayout.builder(layout).contentSlots(0, 0));

    assertThrows(
        IllegalStateException.class,
        () ->
            PaginationLayout.builder(layout)
                .contentSlots(0, 1)
                .previousSlot(0)
                .nextSlot(17)
                .build());

    assertThrows(
        IllegalStateException.class,
        () ->
            PaginationLayout.builder(layout)
                .contentSlots(0, 1)
                .previousSlot(16)
                .nextSlot(16)
                .build());
  }

  @Test
  void rejectsPageWhoseSizeDiffersFromLayout() {
    MenuLayout layout = MenuLayout.chest(2);
    PaginationLayout pagination =
        PaginationLayout.builder(layout)
            .contentSlots(0, 1, 2)
            .previousSlot(16)
            .nextSlot(17)
            .build();

    PageSlice<String> incompatible =
        PageSlice.unknownTotal(PageRequest.first(2), List.of("a"), false);

    assertThrows(
        IllegalArgumentException.class,
        () -> pagination.forEachEntry(incompatible, (slot, entry, index, absoluteIndex) -> {}));
  }
}
