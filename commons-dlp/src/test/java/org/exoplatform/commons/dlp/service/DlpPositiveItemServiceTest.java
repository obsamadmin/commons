package org.exoplatform.commons.dlp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.exoplatform.commons.dlp.dao.AbstractDAOTest;
import org.exoplatform.commons.dlp.dao.DlpPositiveItemDAO;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import org.exoplatform.commons.dlp.service.impl.DlpPositiveItemServiceImpl;
import org.exoplatform.container.PortalContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DlpPositiveItemServiceTest extends AbstractDAOTest {

    private DlpPositiveItemServiceImpl dlpPositiveItemService;

    private DlpPositiveItemDAO dlpPositiveItemDAO;

    @Before
    public void setUp() throws Exception {
        PortalContainer container = PortalContainer.getInstance();
        dlpPositiveItemDAO = container.getComponentInstanceOfType(DlpPositiveItemDAO.class);
        dlpPositiveItemService = new DlpPositiveItemServiceImpl(dlpPositiveItemDAO);
    }

    @Test
    public void testGetDlpPositivesItems() throws Exception {
        //Given
        DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
        dlpPositiveItemEntity.setType("file");
        dlpPositiveItemEntity.setTitle("file");
        dlpPositiveItemEntity.setReference("ref");
        dlpPositiveItemEntity.setDetectionDate(Calendar.getInstance());
        dlpPositiveItemEntity.setAuthor("root");
        dlpPositiveItemEntity.setKeywords("Keywords");

        DlpPositiveItemEntity dlpPositiveItemEntity1 = new DlpPositiveItemEntity();
        dlpPositiveItemEntity1.setType("file1");
        dlpPositiveItemEntity1.setTitle("file1");
        dlpPositiveItemEntity1.setReference("ref1");
        dlpPositiveItemEntity1.setDetectionDate(Calendar.getInstance());
        dlpPositiveItemEntity1.setAuthor("root");
        dlpPositiveItemEntity.setKeywords("Keywords1");

        DlpPositiveItemEntity dlpPositiveItemEntity2 = new DlpPositiveItemEntity();
        dlpPositiveItemEntity2.setType("file2");
        dlpPositiveItemEntity2.setTitle("file2");
        dlpPositiveItemEntity2.setReference("ref2");
        dlpPositiveItemEntity2.setDetectionDate(Calendar.getInstance());
        dlpPositiveItemEntity2.setAuthor("root");
        dlpPositiveItemEntity.setKeywords("Keywords2");

        //When
        dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity);
        dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity1);
        dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity2);

        List<DlpPositiveItem> dlpPositiveItems = dlpPositiveItemService.getDlpPositivesItems(0, 20);
        Long size = dlpPositiveItemService.getDlpPositiveItemsCount();

        //Then 
        assertNotNull(dlpPositiveItems);
        assertEquals(size.intValue(), 3);


        // when
        dlpPositiveItemService.deleteDlpPositiveItem(dlpPositiveItems.get(0).getId());
        size = dlpPositiveItemService.getDlpPositiveItemsCount();

        //then
        assertEquals(size.intValue(), 2);
    }

    @Test
    public void testGetDlpPositiveItemByReference() throws Exception {
        //Given
        DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
        dlpPositiveItemEntity.setType("file");
        dlpPositiveItemEntity.setTitle("file");
        dlpPositiveItemEntity.setDetectionDate(Calendar.getInstance());
        dlpPositiveItemEntity.setAuthor("root");
        dlpPositiveItemEntity.setKeywords("test1");
        dlpPositiveItemEntity.setReference("reference1234");
        //When
        dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity);
        DlpPositiveItem dlpPositiveItem = dlpPositiveItemService.getDlpPositiveItemByReference("reference1234");

        //Then
        assertNotNull(dlpPositiveItem);
    }
}