package br.com.yago.emerald.model.pathfinding;

import br.com.yago.emerald.model.Direction;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;

import java.util.*;

public class Pathfinder {

    private final TiledMapTileLayer collisionLayer;

    private static class Node implements Comparable<Node> {
        int x, y;
        float gCost, hCost, fCost;
        Node parent;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Node other) {
            return Float.compare(this.fCost, other.fCost);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public Pathfinder(TiledMapTileLayer collisionLayer) {
        this.collisionLayer = collisionLayer;
    }

    /**
     * ## MÉTODO ATUALIZADO ##
     * Agora aceita um conjunto de tiles ocupados para evitar durante a busca.
     */
    public Queue<Direction> findPath(Vector2 startTile, Vector2 endTile, Set<Vector2> occupiedTiles) {
        int startX = (int)startTile.x;
        int startY = (int)startTile.y;
        int endX = (int)endTile.x;
        int endY = (int)endTile.y;

        Node startNode = new Node(startX, startY);
        Node endNode = new Node(endX, endY);

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();
            closedSet.add(currentNode);

            if (currentNode.equals(endNode)) {
                return reconstructPath(currentNode);
            }

            for (Direction dir : Direction.values()) {
                int neighborX = currentNode.x + dir.getDx();
                int neighborY = currentNode.y + dir.getDy();

                // Usa a nova verificação que considera os tiles ocupados.
                if (!isWalkable(neighborX, neighborY, occupiedTiles)) {
                    continue;
                }
                
                Node neighborNode = new Node(neighborX, neighborY);
                if (closedSet.contains(neighborNode)) {
                    continue;
                }

                float newMovementCostToNeighbor = currentNode.gCost + 1;
                
                Node existingNode = findNodeInQueue(openSet, neighborNode);
                if (existingNode == null || newMovementCostToNeighbor < existingNode.gCost) {
                    if (existingNode != null) {
                        openSet.remove(existingNode);
                    }
                    
                    neighborNode.gCost = newMovementCostToNeighbor;
                    neighborNode.hCost = getDistance(neighborNode, endNode);
                    neighborNode.fCost = neighborNode.gCost + neighborNode.hCost;
                    neighborNode.parent = currentNode;
                    openSet.add(neighborNode);
                }
            }
        }
        return new Queue<>();
    }
    
    /**
     * ## MÉTODO ATUALIZADO ##
     * Agora considera os tiles ocupados ao procurar o melhor adjacente.
     */
    public Vector2 findBestAdjacent(int startX, int startY, int targetX, int targetY, Set<Vector2> occupiedTiles) {
        Vector2 bestTile = null;
        int bestDist = Integer.MAX_VALUE;

        for (Direction dir : Direction.values()) {
            int adjX = targetX + dir.getDx();
            int adjY = targetY + dir.getDy();
            if (isWalkable(adjX, adjY, occupiedTiles)) {
                int dist = Math.abs(adjX - startX) + Math.abs(adjY - startY);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestTile = new Vector2(adjX, adjY);
                }
            }
        }
        return bestTile;
    }
    
    /**
     * ## MÉTODO ATUALIZADO ##
     * Verifica a camada de colisão do mapa E a lista de tiles ocupados.
     */
    private boolean isWalkable(int x, int y, Set<Vector2> occupiedTiles) {
        if (occupiedTiles.contains(new Vector2(x, y))) {
            return false;
        }
        if (x < 0 || x >= collisionLayer.getWidth() || y < 0 || y >= collisionLayer.getHeight()) {
            return false;
        }
        return collisionLayer.getCell(x, y) == null;
    }

    // --- MÉTODOS INTERNOS (SEM ALTERAÇÃO) ---
    private Queue<Direction> reconstructPath(Node endNode) {
        List<Direction> path = new ArrayList<>();
        Node currentNode = endNode;
        while (currentNode.parent != null) {
            path.add(getDirectionFromNodes(currentNode.parent, currentNode));
            currentNode = currentNode.parent;
        }
        Collections.reverse(path);
        Queue<Direction> finalPath = new Queue<>();
        for(Direction dir : path) {
            finalPath.addLast(dir);
        }
        return finalPath;
    }

    private Direction getDirectionFromNodes(Node from, Node to) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        for (Direction dir : Direction.values()) {
            if (dir.getDx() == dx && dir.getDy() == dy) {
                return dir;
            }
        }
        return null;
    }

    private int getDistance(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
    
    private Node findNodeInQueue(PriorityQueue<Node> queue, Node target) {
        for (Node node : queue) {
            if (node.equals(target)) {
                return node;
            }
        }
        return null;
    }
}