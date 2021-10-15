/*
 * Copyright (c) 2004-2021, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.system.grid;

import static org.hisp.dhis.common.DimensionalObject.DIMENSION_SEP;
import static org.hisp.dhis.system.util.PDFUtils.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.velocity.VelocityContext;
import org.hisp.dhis.common.DimensionalItemObject;
import org.hisp.dhis.common.DimensionalObjectUtils;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.GridHeader;
import org.hisp.dhis.commons.collection.ListUtils;
import org.hisp.dhis.commons.util.Encoder;
import org.hisp.dhis.commons.util.TextUtils;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.system.util.CodecUtils;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.system.velocity.VelocityManager;
import org.hisp.dhis.util.DateUtils;
import org.hisp.staxwax.factory.XMLFactory;
import org.hisp.staxwax.writer.XMLWriter;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.csvreader.CsvWriter;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * @author Lars Helge Overland
 */
@Slf4j
public class GridUtils
{
    private static final String EMPTY = "";

    private static final char CSV_DELIMITER = ',';

    private static final String XLS_SHEET_PREFIX = "Sheet ";

    private static final int JXL_MAX_COLS = 256;

    private static final String FONT_ARIAL = "Arial";

    private static final NodeFilter HTML_ROW_FILTER = new OrFilter( new TagNameFilter( "td" ),
        new TagNameFilter( "th" ) );

    private static final Encoder ENCODER = new Encoder();

    private static final String KEY_GRID = "grid";

    private static final String KEY_ENCODER = "encoder";

    private static final String KEY_PARAMS = "params";

    private static final String JASPER_TEMPLATE = "grid.vm";

    private static final String HTML_TEMPLATE = "grid-html.vm";

    private static final String HTML_CSS_TEMPLATE = "grid-html-css.vm";

    private static final String HTML_INLINE_CSS_TEMPLATE = "grid-html-inline-css.vm";

    private static final String ATTR_GRID = "grid";

    private static final String ATTR_TITLE = "title";

    private static final String ATTR_SUBTITLE = "subtitle";

    private static final String ATTR_WIDTH = "width";

    private static final String ATTR_HEIGHT = "height";

    private static final String ATTR_HEADERS = "headers";

    private static final String ATTR_HEADER = "header";

    private static final String ATTR_NAME = "name";

    private static final String ATTR_COLUMN = "column";

    private static final String ATTR_TYPE = "type";

    private static final String ATTR_HIDDEN = "hidden";

    private static final String ATTR_META = "meta";

    private static final String ATTR_ROWS = "rows";

    private static final String ATTR_ROW = "row";

    private static final String ATTR_FIELD = "field";
    
    private static final String SPACE = " ";
    
    private static final String SIGNOFF_DAILY = "\nSigned of by\nName (PRINT):\t_______________________________________________\t\t\t\tSignature:\t_______________________________________________\t\t\t\tPosition:\t_______________________________________________\t\t\t\tDate:\t_________________________";
    private static final String SIGNOFF_MONTHLY = "\nSigned of by\nName (PRINT):\t__________________________\t\t\t\tSignature:\t__________________________\t\t\t\tPosition:\t__________________________\t\t\t\tDate:\t____________";



    /**
     * Writes a PDF representation of the given Grid to the given OutputStream.
     */
    public static void toPdf( Grid grid, OutputStream out )
    {
        if ( isNonEmptyGrid( grid ) )
        {
            Document document = openDocument( out );

            toPdfInternal( grid, document, 0F );

            addPdfTimestamp( document, true );

            closeDocument( document );
        }
    }

    /**
     * Writes a PDF representation of the given list of Grids to the given
     * OutputStream.
     */
    public static void toPdf( List<Grid> grids, OutputStream out )
    {
        if ( hasNonEmptyGrid( grids ) )
        {
            Document document = openDocument( out );

            for ( Grid grid : grids )
            {
                toPdfInternal( grid, document, 40F );
            }

            addPdfTimestamp( document, false );

            closeDocument( document );
        }
    }
    
    /**
     * Writes a PDF representation of the given list of Grids to the given OutputStream.
     */
    public static void toPdfCustom( List<Grid> grids, OutputStream out, int selectedNoOfSignatures )
    {
        if ( hasNonEmptyGrid( grids ) )
        {            
        	boolean gridsReport = false;
            
            for ( Grid grid : grids )
            {            	
            	System.out.println(grid.getWidth());
            	
            	if(grid.getWidth() > 2){
            		gridsReport  = true;
            		break;
            	}            	
            }
            
            Document document = openDocument( out, gridsReport );
            
            for ( Grid grid : grids )
            {            	
            	if(gridsReport)
            	{            		
            		toPdfInternalDaily( grid, document, 40F );
            	}else{
            		toPdfInternalMontlhy( grid, document, 40F );
            	}
            	
            }

            addPdfTimestampCustom( document, false, selectedNoOfSignatures, gridsReport );

            closeDocument( document );
        }
    }

    private static void toPdfInternal( Grid grid, Document document, float spacing )
    {
        if ( grid == null || grid.getVisibleWidth() == 0 )
        {
            return;
        }

        PdfPTable table = new PdfPTable( grid.getVisibleWidth() );

        table.setHeaderRows( 1 );
        table.setWidthPercentage( 100F );
        table.setKeepTogether( false );
        table.setSpacingAfter( spacing );

        table.addCell( resetPaddings( getTitleCell( grid.getTitle(), grid.getVisibleWidth() ), 0, 30, 0, 0 ) );

        if ( StringUtils.isNotEmpty( grid.getSubtitle() ) )
        {
            table.addCell( getSubtitleCell( grid.getSubtitle(), grid.getVisibleWidth() ) );
            table.addCell( getEmptyCell( grid.getVisibleWidth(), 30 ) );
        }

        for ( GridHeader header : grid.getVisibleHeaders() )
        {
            table.addCell( getItalicCell( header.getColumn() ) );
        }

        table.addCell( getEmptyCell( grid.getVisibleWidth(), 10 ) );

        for ( List<Object> row : grid.getVisibleRows() )
        {
            for ( Object col : row )
            {
                table.addCell( getTextCell( col ) );
            }
        }

        addTableToDocument( document, table );
    }
    
	private static void toPdfInternalMontlhy( Grid grid, Document document, float spacing )
    {
    	if ( grid == null || grid.getVisibleWidth() == 0 )
        {
            return;
        }
        try {
    		
				PdfPTable table = new PdfPTable( grid.getVisibleWidth() );

				int headerwidths[] = getHeaderWidth(grid.getVisibleWidth());
				table.setHeaderRows(4);
				table.setKeepTogether( false );
				table.setWidths(headerwidths);
				table.setWidthPercentage(100);
				table.getDefaultCell().setPadding(3);
				table.getDefaultCell().setBorderWidth(2);
				table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
				
				table.addCell( resetPaddings( getTitleCell( grid.getTitle(), grid.getVisibleWidth() ), 0, 30, 0, 0 ) );
				
				if ( StringUtils.isNotEmpty( grid.getSubtitle() ) )
				{
				    table.addCell( getSubtitleCell( grid.getSubtitle(), grid.getVisibleWidth() ) );
				    table.addCell( getEmptyCell( grid.getVisibleWidth(), 30 ) );
				}
				
				for ( GridHeader header : grid.getVisibleHeaders() )
				{
					table.addCell( header.getName() );
				}				
				
				int i = 1; 
				for ( List<Object> row : grid.getVisibleRows() )
				{
					if (i % 2 == 1) {
						table.getDefaultCell().setGrayFill(0.9f);
					}
				    for ( Object col : row )
				    {
				        String text = col != null ? String.valueOf( col ) : EMPTY;
				        table.addCell( text );
				    }
				    if (i % 2 == 1) {
				    	table.getDefaultCell().setGrayFill(1);
					}
				    
				    i++;
				}
				
				addTableToDocument( document, table );
        } catch (Exception de) {
			de.printStackTrace();
		}
    }
    
    private static void toPdfInternalDaily( Grid grid, Document document, float spacing )
    {
    			
        if ( grid == null || grid.getVisibleWidth() == 0 )
        {
            return;
        }
        try {
    		
				PdfPTable table = new PdfPTable( grid.getVisibleWidth() );

				int headerwidths[] = getHeaderWidth(grid.getVisibleWidth());
				table.setSpacingBefore(15f);
				table.setSpacingAfter(15f);
				table.setHeaderRows(4);
				table.setKeepTogether( false );
				table.setWidths(headerwidths);
				table.setWidthPercentage(100);
				table.getDefaultCell().setPadding(3);
				table.getDefaultCell().setBorderWidth(2);
				table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);				
				
				table.addCell( resetPaddings( getTitleCell( grid.getTitle(), grid.getVisibleWidth() ), 0, 30, 0, 0 ) );
				
				if ( StringUtils.isNotEmpty( grid.getSubtitle() ) )
				{
				    table.addCell( getSubtitleCell( grid.getSubtitle(), grid.getVisibleWidth() ) );
				    table.addCell( getEmptyCell( grid.getVisibleWidth(), 30 ) );
				}
				
				for ( GridHeader header : grid.getVisibleHeaders() )
				{
					if(header.getName().startsWith("input"))
					{
						table.addCell( "Data element" );
					}else{
						table.addCell( header.getName() );
					}
				}				
				
				int i = 1; 
				
				for ( List<Object> row : grid.getVisibleRows() )
				{
					if (i % 2 == 1) {
						table.getDefaultCell().setGrayFill(0.9f);
					}
					
					int j = 1; 
				    for ( Object col : row )
				    {
				    	String text = col != null ? String.valueOf( col ) : EMPTY;
				    	
				    	if (j == 1) {
							table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
							table.addCell( text );
				    		
				    	}else {
				    		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
				    		table.addCell( text );
				    	}				        
				        
				        j++;
				    }
				    if (i % 2 == 1) {
				    	table.getDefaultCell().setGrayFill(1);
					}
				    
				    i++;
				}
				
				addTableToDocument( document, table );
        
	    } catch (Exception de) {
			de.printStackTrace();
		}
    }

    private static void addPdfTimestamp( Document document, boolean paddingTop )
    {
        PdfPTable table = new PdfPTable( 1 );
        table.addCell( getEmptyCell( 1, (paddingTop ? 30 : 0) ) );
        table.addCell( getTextCell( getGeneratedString() ) );
        addTableToDocument( document, table );
    }
    
	private static void addPdfTimestampCustom( Document document, boolean paddingTop, int selectedNoOfSignatures, boolean dailyReport )
    {
    	PdfPTable table = new PdfPTable(1);
        table.addCell( getEmptyCell( 1, ( paddingTop ? 30 : 0 ) ) );
        table.addCell( getTextCell( getGeneratedString() ) );
        
        if(selectedNoOfSignatures >= 1){
        	for(int i= 0; i< selectedNoOfSignatures; i++)
        	{
        		if(dailyReport){
        			table.addCell( getTextCell( SIGNOFF_DAILY ) );
        		}else{
        			table.addCell( getTextCellMonthlyCustom( SIGNOFF_MONTHLY ) );
        		}       		
        	}
        }
        addTableToDocument( document, table );
    } 

    /**
     * Writes a XLS (Excel workbook) representation of the given list of Grids
     * to the given OutputStream.
     */
    public static void toXls( List<Grid> grids, OutputStream out )
        throws Exception
    {
        Workbook workbook = new HSSFWorkbook();

        CellStyle headerCellStyle = createHeaderCellStyle( workbook );
        CellStyle cellStyle = createCellStyle( workbook );

        for ( int i = 0; i < grids.size(); i++ )
        {
            Grid grid = grids.get( i );

            String sheetName = CodecUtils
                .filenameEncode( StringUtils.defaultIfEmpty( grid.getTitle(), XLS_SHEET_PREFIX + (i + 1) ) );

            toXlsInternal( grid, workbook.createSheet( sheetName ), headerCellStyle, cellStyle );
        }

        workbook.write( out );
        workbook.close();
    }

    /**
     * Writes a XLS (Excel workbook) representation of the given Grid to the
     * given OutputStream.
     */
    public static void toXls( Grid grid, OutputStream out )
        throws Exception
    {
        Workbook workbook = new HSSFWorkbook();

        String sheetName = CodecUtils
            .filenameEncode( StringUtils.defaultIfEmpty( grid.getTitle(), XLS_SHEET_PREFIX + 1 ) );

        toXlsInternal( grid, workbook.createSheet( sheetName ), createHeaderCellStyle( workbook ),
            createCellStyle( workbook ) );

        workbook.write( out );
        workbook.close();
    }

    private static void toXlsInternal( Grid grid, Sheet sheet, CellStyle headerCellStyle, CellStyle cellStyle )
    {
        if ( grid == null )
        {
            return;
        }

        int cols = grid.getVisibleHeaders().size();

        if ( cols > JXL_MAX_COLS )
        {
            log.warn(
                "Grid will be truncated, no of columns is greater than JXL max limit: " + cols + "/" + JXL_MAX_COLS );
        }

        int rowNumber = 0;

        int columnIndex = 0;

        if ( StringUtils.isNotEmpty( grid.getTitle() ) )
        {
            Cell cell = sheet.createRow( rowNumber ).createCell( columnIndex, CellType.STRING );
            cell.setCellValue( grid.getTitle() );
            cell.setCellStyle( headerCellStyle );

            rowNumber++;
        }

        if ( StringUtils.isNotEmpty( grid.getSubtitle() ) )
        {
            Cell cell = sheet.createRow( ++rowNumber ).createCell( columnIndex, CellType.STRING );
            cell.setCellValue( grid.getSubtitle() );
            cell.setCellStyle( headerCellStyle );
            rowNumber++;
        }

        List<GridHeader> headers = ListUtils.subList( grid.getVisibleHeaders(), 0, JXL_MAX_COLS );
        Row headerRow = sheet.createRow( ++rowNumber );
        for ( GridHeader header : headers )
        {
            Cell cell = headerRow.createCell( columnIndex++, CellType.STRING );
            cell.setCellStyle( headerCellStyle );
            cell.setCellValue( header.getColumn() );
        }

        rowNumber++;

        for ( List<Object> row : grid.getVisibleRows() )
        {
            Row xlsRow = sheet.createRow( rowNumber );
            xlsRow.setRowStyle( cellStyle );
            columnIndex = 0;

            List<Object> columns = ListUtils.subList( row, 0, JXL_MAX_COLS );

            for ( Object column : columns )
            {
                if ( column != null && MathUtils.isNumeric( String.valueOf( column ) ) )
                {
                    xlsRow.createCell( columnIndex++, CellType.NUMERIC )
                        .setCellValue( Double.parseDouble( String.valueOf( column ) ) );
                }
                else
                {
                    xlsRow.createCell( columnIndex++, CellType.STRING )
                        .setCellValue( column != null ? String.valueOf( column ) : EMPTY );
                }
            }

            rowNumber++;
        }
    }

    /**
     * Writes a CSV representation of the given Grid to the given OutputStream.
     */
    public static void toCsv( Grid grid, Writer writer )
        throws IOException
    {
        if ( grid == null )
        {
            return;
        }

        CsvWriter csvWriter = new CsvWriter( writer, CSV_DELIMITER );

        Iterator<GridHeader> headers = grid.getHeaders().iterator();

        if ( !grid.getHeaders().isEmpty() )
        {
            while ( headers.hasNext() )
            {
                csvWriter.write( headers.next().getColumn() );
            }

            csvWriter.endRecord();
        }

        for ( List<Object> row : grid.getRows() )
        {
            for ( Object value : row )
            {
                csvWriter.write( value != null ? String.valueOf( value ) : StringUtils.EMPTY );
            }

            csvWriter.endRecord();
        }
    }

    /**
     * Writes a Jasper Reports representation of the given Grid to the given
     * OutputStream.
     */
    public static void toJasperReport( Grid grid, Map<String, Object> params, OutputStream out )
        throws Exception
    {
        if ( grid == null )
        {
            return;
        }

        final StringWriter writer = new StringWriter();

        render( grid, params, writer, JASPER_TEMPLATE );

        String report = writer.toString();

        JasperReport jasperReport = JasperCompileManager
            .compileReport( IOUtils.toInputStream( report, StandardCharsets.UTF_8 ) );

        JasperPrint print = JasperFillManager.fillReport( jasperReport, params, grid );

        JasperExportManager.exportReportToPdfStream( print, out );
    }

    /**
     * Writes a JRXML (Jasper Reports XML) representation of the given Grid to
     * the given Writer.
     */
    public static void toJrxml( Grid grid, Map<?, ?> params, Writer writer )
    {
        render( grid, params, writer, JASPER_TEMPLATE );
    }

    /**
     * Writes a HTML representation of the given Grid to the given Writer.
     */
    public static void toHtml( Grid grid, Writer writer )
    {
        render( grid, null, writer, HTML_TEMPLATE );
    }

    /**
     * Writes a HTML representation of the given Grid to the given Writer.
     */
    public static void toHtmlCss( Grid grid, Writer writer )
    {
        render( grid, null, writer, HTML_CSS_TEMPLATE );
    }

    /**
     * Writes a HTML representation of the given Grid to the given Writer.
     */
    public static void toHtmlInlineCss( Grid grid, Writer writer )
    {
        render( grid, null, writer, HTML_INLINE_CSS_TEMPLATE );
    }

    /**
     * Writes an XML representation of the given Grid to the given OutputStream.
     */
    public static void toXml( Grid grid, OutputStream out )
    {
        XMLWriter writer = XMLFactory.getXMLWriter( out );

        writer.openDocument();
        writer.openElement( ATTR_GRID, ATTR_TITLE, grid.getTitle(), ATTR_SUBTITLE, grid.getSubtitle(),
            ATTR_WIDTH, String.valueOf( grid.getWidth() ), ATTR_HEIGHT, String.valueOf( grid.getHeight() ) );

        writer.openElement( ATTR_HEADERS );

        for ( GridHeader header : grid.getHeaders() )
        {
            writer.writeElement( ATTR_HEADER, null, ATTR_NAME, header.getName(), ATTR_COLUMN, header.getColumn(),
                ATTR_TYPE, header.getType(), ATTR_HIDDEN, String.valueOf( header.isHidden() ), ATTR_META,
                String.valueOf( header.isMeta() ) );
        }

        writer.closeElement();
        writer.openElement( ATTR_ROWS );

        for ( List<Object> row : grid.getRows() )
        {
            writer.openElement( ATTR_ROW );

            for ( Object field : row )
            {
                writer.writeElement( ATTR_FIELD, field != null ? String.valueOf( field ) : EMPTY );
            }

            writer.closeElement();
        }

        writer.closeElement();
        writer.closeElement();

        writer.closeDocument();
    }

    /**
     * Writes all rows in the SqlRowSet to the given Grid.
     */
    public static void addRows( Grid grid, SqlRowSet rs )
    {
        int cols = rs.getMetaData().getColumnCount();

        while ( rs.next() )
        {
            grid.addRow();

            for ( int i = 1; i <= cols; i++ )
            {
                grid.addValue( rs.getObject( i ) );
            }
        }
    }

    /**
     * Derives the positional index of a Grid's row, based on the
     * {@see DimensionalItemObject} identifiers
     *
     * @param row a Grid's row
     * @param items a List of {@see DimensionalItemObject}
     * @param defaultIndex the default positional index to return
     * @return the positional index matching one of the DimensionalItemObject
     *         identifiers
     */
    public static int getGridIndexByDimensionItem( List<Object> row, List<DimensionalItemObject> items,
        int defaultIndex )
    {
        // accumulate the DimensionalItemObject identifiers into a List
        List<String> valid = items.stream().map( DimensionalItemObject::getDimensionItem )
            .collect( Collectors.toList() );

        // skip the last index, since it is always the row value
        for ( int i = 0; i < row.size() - 1; i++ )
        {
            final String value = (String) row.get( i );
            if ( valid.contains( value ) )
            {
                return i;
            }
        }
        return defaultIndex;
    }

    /**
     * Creates a list of Grids based on the given HTML string. This works only
     * for table-based HTML documents.
     *
     * @param html the HTML string.
     * @param title the title to use for the grids.
     * @return a list of Grids.
     */
    public static List<Grid> fromHtml( String html, String title, Period period, OrganisationUnit unit, I18nFormat format )
        throws Exception
    {
        if ( html == null || html.trim().isEmpty() )
        {
            return null;
        }

        List<Grid> grids = new ArrayList<>();

        Parser parser = Parser.createParser( html, "UTF-8" );

        Node[] tables = parser.extractAllNodesThatMatch( new TagNameFilter( "table" ) ).toNodeArray();

        for ( Node t : tables )
        {
            Grid grid = new ListGrid();

            grid.setTitle( title );
            grid.setSubtitle( unit.getName() + SPACE + format.formatPeriod( period ) );

            TableTag table = (TableTag) t;

            TableRow[] rows = table.getRows();

            Integer firstColumnCount = null;

            for ( TableRow row : rows )
            {
                if ( getColumnCount( row ) == 0 ) // Ignore if no cells
                {
                    log.warn( "Ignoring row with no columns" );
                    continue;
                }

                Node[] cells = row.getChildren().extractAllNodesThatMatch( HTML_ROW_FILTER ).toNodeArray();

                if ( firstColumnCount == null ) // First row becomes header
                {
                    firstColumnCount = getColumnCount( row );

                    for ( Node c : cells )
                    {
                        TagNode cell = (TagNode) c;

                        grid.addHeader( new GridHeader( getValue( cell ), false, false ) );

                        Integer colSpan = MathUtils.parseInt( cell.getAttribute( "colspan" ) );

                        if ( colSpan != null && colSpan > 1 )
                        {
                            grid.addEmptyHeaders( (colSpan - 1) );
                        }
                    }
                }
                else // Rest becomes rows
                {
                    if ( firstColumnCount != getColumnCount( row ) ) // Ignore
                    {
                        log.warn( "Ignoring row which has " + row.getColumnCount() + " columns since table has "
                            + firstColumnCount + " columns" );
                        continue;
                    }

                    grid.addRow();

                    for ( Node c : cells )
                    {
                        // TODO row span

                        TagNode cell = (TagNode) c;

                        grid.addValue( getValue( cell ) );

                        Integer colSpan = MathUtils.parseInt( cell.getAttribute( "colspan" ) );

                        if ( colSpan != null && colSpan > 1 )
                        {
                            grid.addEmptyValues( (colSpan - 1) );
                        }
                    }
                }
            }

            grids.add( grid );
        }

        return grids;
    }

    /**
     * Returns the number of columns/cells in the given row, including cell
     * spacing.
     */
    private static int getColumnCount( TableRow row )
    {
        Node[] cells = row.getChildren().extractAllNodesThatMatch( HTML_ROW_FILTER ).toNodeArray();

        int cols = 0;

        for ( Node cell : cells )
        {
            Integer colSpan = MathUtils.parseInt( ((TagNode) cell).getAttribute( "colspan" ) );

            cols += colSpan != null ? colSpan : 1;
        }

        return cols;
    }

    /**
     * Retrieves the value of a table cell. Appends the text of child nodes of
     * the cell. In case of composite tags like span or div the inner text is
     * appended.
     */
    public static String getValue( TagNode cell )
    {
        StringBuilder builder = new StringBuilder();

        for ( Node child : cell.getChildren().toNodeArray() )
        {
            if ( child instanceof CompositeTag )
            {
                builder.append( ((CompositeTag) child).getStringText() );
            }
            else
            {
                builder.append( child.getText() );
            }
        }

        return builder.toString().trim().replaceAll( "&nbsp;", EMPTY );
    }

    /**
     * Returns a mapping based on the given grid where the key is a joined
     * string of the string value of each value for meta columns. The value is
     * the object at the given value index. The map contains at maximum one
     * entry per row in the given grid, less if the joined key string are
     * duplicates. The object at the value index must be numeric.
     *
     * @param grid the grid.
     * @param valueIndex the index of the column holding the value, must be
     *        numeric.
     * @return a meta string to value object mapping.
     */
    public static Map<String, Object> getMetaValueMapping( Grid grid, int valueIndex )
    {
        Map<String, Object> map = new HashMap<>();

        List<Integer> metaIndexes = grid.getMetaColumnIndexes();

        for ( List<Object> row : grid.getRows() )
        {
            List<Object> metaDataRowItems = ListUtils.getAtIndexes( row, metaIndexes );

            String key = TextUtils.join( metaDataRowItems, DIMENSION_SEP, DimensionalObjectUtils.NULL_REPLACEMENT );

            map.put( key, row.get( valueIndex ) );
        }

        return map;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    /**
     * Returns a string indicating when the grid was generated.
     */
    private static String getGeneratedString()
    {
        return "Generated: " + DateUtils.getMediumDateString();
    }

    /**
     * Render using Velocity.
     */
    private static void render( Grid grid, Map<?, ?> params, Writer writer, String template )
    {
        final VelocityContext context = new VelocityContext();

        context.put( KEY_GRID, grid );
        context.put( KEY_ENCODER, ENCODER );
        context.put( KEY_PARAMS, params );

        new VelocityManager().getEngine().getTemplate( template ).merge( context, writer );
    }

    /**
     * Indicates whether the given list of grids have at least one grid which is
     * not null and has more than zero visible columns.
     */
    private static boolean hasNonEmptyGrid( List<Grid> grids )
    {
        if ( grids != null && grids.size() > 0 )
        {
            for ( Grid grid : grids )
            {
                if ( isNonEmptyGrid( grid ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Indicates whether grid is not null and has more than zero visible
     * columns.
     */
    private static boolean isNonEmptyGrid( Grid grid )
    {
        return grid != null && grid.getVisibleWidth() > 0;
    }

    private static CellStyle createHeaderCellStyle( Workbook workbook )
    {
        CellStyle headerCellStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold( true );
        headerFont.setFontHeightInPoints( (short) 10 );
        headerFont.setFontName( FONT_ARIAL );
        headerCellStyle.setFont( headerFont );
        return headerCellStyle;
    }

    private static CellStyle createCellStyle( Workbook workbook )
    {
        CellStyle cellStyle = workbook.createCellStyle();
        Font cellFont = workbook.createFont();
        cellFont.setFontHeightInPoints( (short) 10 );
        cellFont.setFontName( FONT_ARIAL );
        cellStyle.setFont( cellFont );
        return cellStyle;
    }
}
