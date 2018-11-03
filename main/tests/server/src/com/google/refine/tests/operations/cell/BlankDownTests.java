package com.google.refine.tests.operations.cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.google.refine.ProjectManager;
import com.google.refine.browsing.EngineConfig;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Column;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.google.refine.operations.OperationRegistry;
import com.google.refine.operations.cell.BlankDownOperation;
import com.google.refine.operations.cell.FillDownOperation;
import com.google.refine.process.Process;
import com.google.refine.tests.RefineTest;
import com.google.refine.tests.util.TestUtils;
import com.google.refine.util.Pool;

public class BlankDownTests extends RefineTest {
    
    Project project = null;
    
    @BeforeSuite
    public void registerOperation() {
        OperationRegistry.registerOperation(getCoreModule(), "blank-down", BlankDownOperation.class);
    }
    
    @BeforeMethod
    public void setUp() {
        project = createCSVProject(
                "key,first,second\n"+
                "a,b,c\n"+
                ",d,c\n"+
                "e,f,c\n"+
                ",,c\n");
    }
    
    @AfterMethod
    public void tearDown() {
        ProjectManager.singleton.deleteProject(project.id);
    }
    
    @Test
    public void serializeBlankDownOperation() throws JSONException, Exception {
        String json = "{\"op\":\"core/blank-down\","
                + "\"description\":\"Blank down cells in column my column\","
                + "\"engineConfig\":{\"mode\":\"record-based\",\"facets\":[]},"
                + "\"columnName\":\"my column\"}";
        AbstractOperation op = BlankDownOperation.reconstruct(project, new JSONObject(json));
        TestUtils.isSerializedTo(op, json);
    }
    
    @Test
    public void testBlankDownRecords() throws Exception {
        AbstractOperation op = new BlankDownOperation(
                EngineConfig.reconstruct(new JSONObject("{\"mode\":\"record-based\",\"facets\":[]}")),
                "second");
        Process process = op.createProcess(project, new Properties());
        process.performImmediate();
        
        Assert.assertEquals("c", project.rows.get(0).cells.get(2).value);
        Assert.assertNull(project.rows.get(1).cells.get(2));
        Assert.assertEquals("c", project.rows.get(2).cells.get(2).value);
        Assert.assertNull(project.rows.get(3).cells.get(2));
    }
    
    @Test
    public void testBlankDownRows() throws Exception {
        AbstractOperation op = new BlankDownOperation(
                EngineConfig.reconstruct(new JSONObject("{\"mode\":\"row-based\",\"facets\":[]}")),
                "second");
        Process process = op.createProcess(project, new Properties());
        process.performImmediate();
        
        Assert.assertEquals("c", project.rows.get(0).cells.get(2).value);
        Assert.assertNull(project.rows.get(1).cells.get(2));
        Assert.assertNull(project.rows.get(2).cells.get(2));
        Assert.assertNull(project.rows.get(3).cells.get(2));
    }
    
    @Test
    public void testKeyColumnIndex() throws Exception {
    	// Shift all column indices
    	for(Row r : project.rows) {
    		r.cells.add(0, null);
    	}
    	List<Column> newColumns = new ArrayList<>();
    	for(Column c : project.columnModel.columns) {
    		newColumns.add(new Column(c.getCellIndex()+1, c.getName()));
    	}
    	project.columnModel.columns.clear();
    	project.columnModel.columns.addAll(newColumns);
    	project.columnModel.update();
    	
    	AbstractOperation op = new BlankDownOperation(
                EngineConfig.reconstruct(new JSONObject("{\"mode\":\"record-based\",\"facets\":[]}")),
                "second");
        Process process = op.createProcess(project, new Properties());
        process.performImmediate();
        //project.saveToOutputStream(System.out, new Pool());
        
        Assert.assertEquals("c", project.rows.get(0).cells.get(3).value);
        Assert.assertNull(project.rows.get(1).cells.get(3));
        Assert.assertEquals("c", project.rows.get(2).cells.get(3).value);
        Assert.assertNull(project.rows.get(3).cells.get(3));
    }
}
