package edu.nju.seg.isd.sisd.ast;

import edu.nju.seg.isd.sisd.ast.expression.Positive;
import edu.nju.seg.isd.sisd.ast.expression.Separation;
import edu.nju.seg.isd.sisd.ast.expression.Unrestricted;
import edu.nju.seg.isd.sisd.ast.expression.Variable;
import io.vavr.Tuple3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NotNull
public final class IntFragment extends BoundFragment {

    private int min = 1;

    private int max = 1;

    private Separation separation = new Unrestricted();

    private List<Variable> masks = new ArrayList<>();

    public IntFragment(int priority,
                       @NotNull List<Item> items,
                       @Nullable Tuple3<Integer, Integer, Positive> bound,
                       @Nullable List<Variable> masks) {
        super(priority, items);
        if (bound != null) {
            this.min = bound._1();
            this.max = bound._2();
            if (bound._3() != null) {
                this.separation = bound._3();
            }
        }
        if (masks != null) {
            this.masks = masks;
        }
    }


    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @NotNull
    public List<Variable> getMasks() {
        return masks;
    }

    @NotNull
    public Separation getSeparation() {
        return separation;
    }

    @Override
    public String uniqueName() {
        return name() + "_" + this.hashCode();
    }

    @Override
    public String name() {
        return "int";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IntFragment that = (IntFragment) o;
        return min == that.min && max == that.max && Objects.equals(separation, that.separation) && Objects.equals(masks, that.masks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), min, max, separation, masks);
    }

}
