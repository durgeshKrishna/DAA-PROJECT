import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

class Airport implements Comparable<Airport> {
    String id;
    Map<Airport, Integer> adjacent;
    int distance;
    Airport parent;
    Point position;
    ImageIcon icon;

    // Constructor
    public Airport(String id, Point position, String iconPath) {
        this.id = id;
        this.position = position;
        this.adjacent = new HashMap<>();
        this.distance = Integer.MAX_VALUE;
        this.parent = null;
        this.icon = new ImageIcon(iconPath);
        // Resize the icon to fit the node size
        this.icon = new ImageIcon(this.icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
    }

    // Method to add a flight to another airport with a distance
    public void addFlight(Airport destination, int distance) {
        adjacent.put(destination, distance);
    }

    // Override compareTo method
    @Override
    public int compareTo(Airport other) {
        return Integer.compare(this.distance, other.distance);
    }
}

class AirportPanel extends JPanel {
    private java.util.List<Airport> airports;
    private java.util.List<Airport> path;
    private final int nodeSize = 20;
    private final int moveStep = 2;
    private int currentStep = 0;
    private javax.swing.Timer timer;
    private Map<Airport, Integer> scores;
    private boolean destinationReached = false;
    private ImageIcon airplaneIcon;
    private int x; // X coordinate of specified point
    private int y; // Y coordinate of specified point
    private double threshold; // Distance threshold for considering nodes as "near"

    public AirportPanel(java.util.List<Airport> airports, java.util.List<Airport> path, int x, int y, double threshold) {
        this.airports = airports;
        this.path = path;
        this.scores = new HashMap<>();
        this.timer = new javax.swing.Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveAirports();
                checkScores();
                repaint();
            }
        });
        this.airplaneIcon = new ImageIcon("airplane.png"); // Path to airplane image
        this.x = x;
        this.y = y;
        this.threshold = threshold;
        timer.start();
    }

    private void moveAirports() {
        if (!destinationReached && currentStep < path.size() - 1) {
            Airport from = path.get(currentStep);
            Airport to = path.get(currentStep + 1);
            Point fromPos = from.position;
            Point toPos = to.position;
            double dx = toPos.x - fromPos.x;
            double dy = toPos.y - fromPos.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double ratio = moveStep / distance;
            fromPos.x += (int) (dx * ratio);
            fromPos.y += (int) (dy * ratio);
            if (fromPos.distance(toPos) < moveStep) {
                currentStep++;
            }
            if (currentStep == path.size() - 1) {
                destinationReached = true;
                showTotalValueCovered();
            }
        }
    }

    private void checkScores() {
        for (Airport airport : airports) {
            Integer score = scores.get(airport);
            if (score != null && score > 0) {
                score--;
                scores.put(airport, score);
            }
        }
    }

    public void addScore(Airport airport, int value) {
        scores.put(airport, scores.getOrDefault(airport, 0) + value);
    }

    private void showTotalValueCovered() {
        int totalValue = 0;
        for (Airport airport : path) {
            totalValue += airport.distance;
        }
        JOptionPane.showMessageDialog(null, "Total value covered: " + totalValue + " km", "Total Value Covered", JOptionPane.INFORMATION_MESSAGE);
    }
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    
    // Set background color to sky blue
    g2d.setColor(new Color(135, 206, 235)); // RGB values for sky blue
    
    // Fill the entire panel with sky blue color
    g2d.fillRect(0, 0, getWidth(), getHeight());
    
    // Draw flights and highlight shortest path
    g2d.setColor(Color.RED);
    for (Airport airport : airports) {
        Point from = airport.position;
        for (Map.Entry<Airport, Integer> entry : airport.adjacent.entrySet()) {
            Airport destination = entry.getKey();
            Point to = destination.position;
            // Draw line between airports
            g2d.drawLine(from.x, from.y, to.x, to.y);
            // Draw text label for distance
            g2d.drawString(Integer.toString(entry.getValue()), (from.x + to.x) / 2, (from.y + to.y) / 2);
            // Draw airplane icon for flights
            int iconWidth = airplaneIcon.getIconWidth();
            int iconHeight = airplaneIcon.getIconHeight();
            int midX = (from.x + to.x) / 2 - iconWidth / 2;
            int midY = (from.y + to.y) / 2 - iconHeight / 2;
            airplaneIcon.paintIcon(this, g2d, midX, midY);
        }
    }

    // Draw airports with icons and labels
    for (Airport airport : airports) {
        Point position = airport.position;
        // Determine the distance from the airport node to the specified point
        double distance = position.distance(x, y); // (x, y) is the specified point
        if (distance < threshold) { // Adjust threshold as needed
            g2d.setColor(Color.BLACK); // Change color to black
        } else {
            g2d.setColor(Color.BLUE); // Default color
        }
        airport.icon.paintIcon(this, g2d, position.x - nodeSize / 2, position.y - nodeSize / 2);
        // Display the name of the place near the airport node
        g2d.drawString(airport.id, position.x + nodeSize, position.y - nodeSize); // Adjust label position as needed
    }
}

}

public class DijkstraAirport extends JFrame {
    private AirportPanel airportPanel;

    public DijkstraAirport(java.util.List<Airport> airports, java.util.List<Airport> path, int x, int y, double threshold) {
        setTitle("Airport Map");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        airportPanel = new AirportPanel(airports, path, x, y, threshold);
        add(airportPanel);
    }

    public static java.util.List<Airport> dijkstra(Airport source, Airport destination) {
        source.distance = 0;
        PriorityQueue<Airport> queue = new PriorityQueue<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            Airport current = queue.poll();

            if (current == destination) {
                java.util.List<Airport> path = new ArrayList<>();
                while (current != null) {
                    path.add(current);
                    current = current.parent;
                }
                Collections.reverse(path);
                return path;
            }

            for (Map.Entry<Airport, Integer> entry : current.adjacent.entrySet()) {
                Airport neighbor = entry.getKey();
                int distance = entry.getValue();
                int distanceThroughCurrent = current.distance + distance;

                if (distanceThroughCurrent < neighbor.distance) {
                    neighbor.distance = distanceThroughCurrent;
                    neighbor.parent = current;
                    queue.add(neighbor);
                }
            }
        }

        return null;
    }

    public static void main(String[] args) {
        // Creating airport instances with positions and icons
        Airport DEL = new Airport("DELHI", new Point(50, 50), "airport.png");
        Airport BOM = new Airport("BOMBAY", new Point(200, 100), "airport.png");
        Airport BLR = new Airport("BANGALORE", new Point(100, 200), "airport.png");
        Airport HYD = new Airport("HYDERABAD", new Point(300, 300), "airport.png");
        Airport MAA = new Airport("CHENNAI", new Point(400, 200), "airport.png");

        // Adding flights and distances
        DEL.addFlight(BOM, 1100);
        DEL.addFlight(BLR, 1400);
        DEL.addFlight(HYD, 1250);
        DEL.addFlight(MAA, 1750);

        BOM.addFlight(BLR, 830);
        BOM.addFlight(HYD, 710);
        BOM.addFlight(MAA, 1040);

        BLR.addFlight(HYD, 500);
        BLR.addFlight(MAA, 290);

        HYD.addFlight(MAA, 620);

        // Prompt the user to input the source and destination airports
        String sourceId = JOptionPane.showInputDialog(null, "Enter source airport (e.g., DELHI, BOMBAY, BANGALORE, HYDERABAD, CHENNAI):");
        String destinationId = JOptionPane.showInputDialog(null, "Enter destination airport(e.g., DELHI, BOMBAY, BANGALORE, HYDERABAD, CHENNAI):");

        // Find the source and destination airports from their IDs
        Airport source = null;
        Airport destination = null;
        for (Airport airport : Arrays.asList(DEL, BOM, BLR, HYD, MAA)) {
            if (airport.id.equals(sourceId)) {
                source = airport;
            }
            if (airport.id.equals(destinationId)) {
                destination = airport;
            }
        }

        if (source != null && destination != null) {
            // Running Dijkstra's algorithm
            java.util.List<Airport> path = dijkstra(source, destination);

            if (path != null) {
                SwingUtilities.invokeLater(() -> {
                    DijkstraAirport airportMap = new DijkstraAirport(Arrays.asList(DEL, BOM, BLR, HYD, MAA), path, 300, 200, 100);
                    airportMap.setVisible(true);
                    // Simulate scoring
                    java.util.Timer scoreTimer = new java.util.Timer();
                    scoreTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            Random rand = new Random();
                            int index = rand.nextInt(path.size());
                            airportMap.airportPanel.addScore(path.get(index), 10);
                        }
                    }, 1000, 2000); // Change the interval as needed
                });
            } else {
                JOptionPane.showMessageDialog(null, "No path found between the source and destination airports.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Invalid source or destination airport ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
