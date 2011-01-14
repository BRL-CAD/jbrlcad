
import java.util.SortedSet;
import org.brlcad.geometry.BrlcadDb;
import org.brlcad.geometry.Partition;
import org.brlcad.geometry.SimpleOverlapHandler;
import org.brlcad.numerics.Point;
import org.brlcad.numerics.Ray;
import org.brlcad.numerics.Vector3;
import org.brlcad.spacePartition.PreppedDb;
import org.junit.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jra
 */
public class ShootTest {
    
    public ShootTest() {
    }

    @Test
    public void testShoot() {
        try {
            BrlcadDb brlcadDb = new BrlcadDb("src/test/resources/test.g");
			PreppedDb prepped = new PreppedDb( brlcadDb, "r1" );
            Point start = new Point(12795.692849, 8897.447508, 8166.793304);
            Vector3 dir = new Vector3(-0.7424, -0.5198, -0.4226);
            Ray ray = new Ray(start, dir);
            SortedSet<Partition> parts = prepped.shootRay(ray, new SimpleOverlapHandler());
            if( parts.size() == 0 ) {
                System.out.println( "MISSED");
            } else {
                for (Partition part : parts) {
                    System.out.println(part);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
