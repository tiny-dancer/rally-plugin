package com.jenkins.plugins.rally.service;

import com.jenkins.plugins.rally.RallyAssetNotFoundException;
import com.jenkins.plugins.rally.RallyException;
import com.jenkins.plugins.rally.config.AdvancedConfiguration;
import com.jenkins.plugins.rally.config.RallyConfiguration;
import com.jenkins.plugins.rally.connector.RallyConnector;
import com.jenkins.plugins.rally.connector.RallyUpdateData;
import com.jenkins.plugins.rally.scm.ScmConnector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RallyServiceTest {

    @Mock
    private ScmConnector scmConnector;

    @Mock
    private RallyConnector connector;

    @Mock
    private RallyConfiguration rallyConfiguration;

    private RallyService service;

    @Before
    public void setUp() throws Exception {
        this.service = new RallyService(this.connector, scmConnector, new AdvancedConfiguration("http://proxy.url"), this.rallyConfiguration);
    }

    @Test(expected=RallyAssetNotFoundException.class)
    public void shouldThrowErrorIfAttemptToUpdateWithoutValidStoryRef() throws Exception {
        when(this.connector.queryForStory("US12345")).thenThrow(new RallyAssetNotFoundException());

        RallyUpdateData details = new RallyUpdateData();
        details.setTaskID("TA54321");
        details.addId("US12345");

        this.service.updateRallyTaskDetails(details);
    }

    @Test
    public void shouldBeConfigurableToCreateNonExistentRepository() throws RallyException {
        when(this.connector.queryForRepository()).thenThrow(new RallyAssetNotFoundException());
        when(this.rallyConfiguration.shouldCreateIfAbsent()).thenReturn(true);

        RallyUpdateData details = new RallyUpdateData();
        details.addId("US12345");
        details.setFilenamesAndActions(new ArrayList<RallyUpdateData.FilenameAndAction>());
        this.service.updateChangeset(details);

        verify(this.connector).createRepository();
    }

    @Test(expected = RallyAssetNotFoundException.class)
    public void shouldBeConfigurableToNotCreateNonExistentRepository() throws RallyException {
        when(this.connector.queryForRepository()).thenThrow(new RallyAssetNotFoundException());
        when(this.rallyConfiguration.shouldCreateIfAbsent()).thenReturn(false);

        RallyUpdateData details = new RallyUpdateData();
        details.setFilenamesAndActions(new ArrayList<RallyUpdateData.FilenameAndAction>());
        this.service.updateChangeset(details);

        verify(this.connector, never()).createRepository();
    }
}
