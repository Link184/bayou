package dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DSubTree extends DASTNode {

    final String node = "DSubTree";
    final List<DASTNode> _nodes;

    public DSubTree() {
        _nodes = new ArrayList<>();
    }

    public DSubTree(List<DASTNode> _nodes) {
        this._nodes = _nodes;
    }

    public void addNode(DASTNode node) {
        _nodes.add(node);
    }

    public void addNodes(List<DASTNode> otherNodes) {
        _nodes.addAll(otherNodes);
    }

    public boolean isValid() {
        return !_nodes.isEmpty();
    }

    @Override
    public void updateSequences(List<Sequence> soFar) {
        for (DASTNode node : _nodes)
            node.updateSequences(soFar);
    }

    public List<DAPICall> getNodesAsCalls() {
        List<DAPICall> calls = new ArrayList<>();
        for (DASTNode node : _nodes) {
            assert node instanceof DAPICall : "invalid branch condition";
            calls.add((DAPICall) node);
        }
        return calls;
    }

    public List<DASTNode> getNodes() {
        return _nodes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || ! (o instanceof DSubTree))
            return false;
        DSubTree tree = (DSubTree) o;
        return _nodes.equals(tree.getNodes());
    }

    @Override
    public int hashCode() {
        return _nodes.hashCode();
    }

    @Override
    public String toString() {
        List<String> _nodesStr = _nodes.stream().map(node -> node.toString()).collect(Collectors.toList());
        return String.join("\n", _nodesStr);
    }
}