package lld.ch17.jigsaw;

/** Outcome of trying to put a piece on a cell. Everything except OK leaves the board unchanged. */
public enum PlaceResult {
    OK,
    OUT_OF_BOUNDS,       // the cell is not on the board
    CELL_OCCUPIED,       // another piece is already there
    ALREADY_PLACED,      // this piece is already somewhere on the board
    EDGE_MUST_BE_FLAT,   // a side facing outside the picture is a tab or a blank
    FLAT_FACES_INSIDE,   // a flat side faces another cell
    SIDES_DO_NOT_FIT     // a side does not interlock with a neighbour that is already placed
}
