package fridgets;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Optional;
import sodium.*;

public class FrFlow extends Fridget {
    public enum Direction { HORIZONTAL, VERTICAL };

    public FrFlow(Direction dir, Collection<Fridget> fridgets) {
        super((size, sMouse, sKey, focus, idSupply) -> {
            Cell<Dimension> desiredSize = new Cell<>(new Dimension(0,0));
            Cell<Drawable> drawable = new Cell<>(new Drawable());
            Stream<Long> sChangeFocus = new Stream<Long>();
            for (Fridget fridget : fridgets) {
                CellLoop<Optional<Dimension>> childSz = new CellLoop<>();
                Fridget.Output fo = new FrTranslate(fridget,
                  dir == Direction.HORIZONTAL
                    ? desiredSize.map(dsz -> new Dimension(dsz.width, 0))
                    : desiredSize.map(dsz -> new Dimension(0, dsz.height)))
                    .reify(childSz, sMouse, sKey, focus, idSupply.child2());
                idSupply = idSupply.child1();
                childSz.loop(
                    Cell.lift((osz, foDsz) ->
                        osz.isPresent()
                            ? Optional.of(dir == Direction.HORIZONTAL
                                ? new Dimension(foDsz.width, osz.get().height)
                                : new Dimension(osz.get().width, foDsz.height))
                            : Optional.empty(),
                        size, fo.desiredSize
                    )
                );
                desiredSize = Cell.lift(
                    dir == Direction.HORIZONTAL
                        ? (dsz, foDsz) -> new Dimension(
                            dsz.width + foDsz.width,
                            dsz.height > foDsz.height ? dsz.height
                                                      : foDsz.height)
                        : (dsz, foDsz) -> new Dimension(
                            dsz.width > foDsz.width ? dsz.width
                                                    : foDsz.width,
                            dsz.height + foDsz.height),
                    desiredSize, fo.desiredSize);
                drawable = Cell.lift(
                    (drA, drB) -> drA.append(drB),
                    drawable, fo.drawable);
                sChangeFocus = sChangeFocus.merge(fo.sChangeFocus);
            }
            return new Fridget.Output(drawable, desiredSize, sChangeFocus);
        });
    }
}
