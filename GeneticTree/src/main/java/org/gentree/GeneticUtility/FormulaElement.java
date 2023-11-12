package org.gentree.GeneticUtility;

/**
 * This class is a part of the genetic tree algorithm.
 * It represents the smallest formula object, operator, variable or integer.
 * It is used to build the tree structure.
 * It is a prefix notation, has a parent left child and right child.
 * It has a reference to the strategy object it belongs to.
 */
public class FormulaElement {


    private FormulaElement left = null;
    private FormulaElement right = null;
    private FormulaElement parent = null;
    private FormulaElementType type = null;
    private String element = null;
    private int maxDepth = 0;
    private Strategy ownerStrategy = null;


    public FormulaElement(FormulaElement left, FormulaElement right, FormulaElement parent, FormulaElementType type,
                          String element) {
        super();
        this.left = left;
        this.right = right;
        this.parent = parent;
        this.type = type;
        this.element = element;
        this.maxDepth = 0;
    }

    public FormulaElement(FormulaElement parent, FormulaElementType type, String element) {
        this.parent = parent;
        this.left = null;
        this.right = null;

        this.type = type;
        this.element = element;
        this.maxDepth = 0;
    }

    public FormulaElement getParent() {
        return parent;
    }

    public void setParent(FormulaElement parent) {
        this.parent = parent;
    }

    public FormulaElementType getType() {
        return type;
    }

    public void setType(FormulaElementType type) {
        this.type = type;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {

        this.element = element;
    }

    public FormulaElement getLeft() {
        return left;
    }

    public void setLeft(FormulaElement left) {
        left.setParent(this);
        this.left = left;
    }

    public FormulaElement getRight() {
        return right;
    }

    public void setRight(FormulaElement right) {
        right.setParent(this);
        this.right = right;
    }

    public Strategy getOwnerStrategy() {
        return ownerStrategy;
    }

    public void setOwnerStrategy(Strategy ownerStrategy) {
        this.ownerStrategy = ownerStrategy;
    }

    public boolean swapChild(FormulaElement changeThis, FormulaElement swapThis) {
        if (changeThis.getElement().compareTo(this.getLeft().getElement()) == 0) {
            this.setLeft(swapThis);
            return true;
        }

        if (changeThis.getElement().compareTo(this.getRight().getElement()) == 0) {
            this.setRight(swapThis);
            return true;
        }
        System.out.println(changeThis.getParent().getElement() + " ---- " + this.getElement());
        System.out.println(changeThis.getElement() + " ---> " + this.getRight().getElement() + " OR " + this.getLeft().getElement());

        return false;
    }

    public FormulaElement safeCopy() {
        return new FormulaElement(this.getLeft(), this.getRight(), this.getParent(), this.getType(), this.getElement());
    }

    public FormulaElement deepCopy() {
        FormulaElement myCopy = new FormulaElement(null, null, this.getParent(), this.getType(), this.getElement());
        if (this.getLeft() != null) {
            myCopy.setLeft(this.getLeft().deepCopy());
        }
        if (this.getRight() != null) {
            myCopy.setRight(this.getRight().deepCopy());
        }
        return myCopy;
    }


    /**
     * Calculates the maximum depth of the tree.
     */
    public int calculateDepth() {
        int l = 0, r = 0;
        if (this.getLeft() != null)
            l = this.getLeft().calculateDepth();
        if (this.getRight() != null)
            r = this.getRight().calculateDepth();
        if (l > r)
            return (l + 1);
        else
            return (r + 1);
    }
}
