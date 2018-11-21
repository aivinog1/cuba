/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spec.cuba.web

import com.haulmont.cuba.core.app.ConfigStorageService
import com.haulmont.cuba.core.app.PersistenceManagerService
import com.haulmont.cuba.core.global.*
import com.haulmont.cuba.gui.UiComponents
import com.haulmont.cuba.gui.model.DataComponents
import com.haulmont.cuba.gui.theme.ThemeConstants
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory
import com.haulmont.cuba.web.App
import com.haulmont.cuba.web.AppUI
import com.haulmont.cuba.web.Connection
import com.haulmont.cuba.web.DefaultApp
import com.haulmont.cuba.web.sys.AppCookies
import com.haulmont.cuba.web.testsupport.TestContainer
import com.haulmont.cuba.web.testsupport.TestServiceProxy
import com.vaadin.server.VaadinSession
import com.vaadin.ui.ConnectorTracker
import com.vaadin.ui.UI
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

class WebSpec extends Specification {

    @Shared @ClassRule
    TestContainer cont = TestContainer.Common.INSTANCE

    Metadata metadata
    MetadataTools metadataTools
    ViewRepository viewRepository
    EntityStates entityStates
    DataManager dataManager
    DataComponents dataComponents
    ComponentsFactory componentsFactory
    UiComponents uiComponents

    AppUI vaadinUi

    @SuppressWarnings("GroovyAccessibility")
    void setup() {
        metadata = cont.getBean(Metadata)
        metadataTools = cont.getBean(MetadataTools)
        viewRepository = cont.getBean(ViewRepository)
        entityStates = cont.getBean(EntityStates)
        dataManager = cont.getBean(DataManager)
        dataComponents = cont.getBean(DataComponents)
        componentsFactory = cont.getBean(ComponentsFactory)
        uiComponents = cont.getBean(UiComponents)

        // all the rest is required for web components

        TestServiceProxy.mock(PersistenceManagerService, Mock(PersistenceManagerService) {
            isNullsLastSorting() >> false
        })

        TestServiceProxy.mock(ConfigStorageService, Mock(ConfigStorageService) {
            getDbProperties() >> [:]
        })

        App app = new TestApp()
        app.cookies = new AppCookies()

        def connection = Mock(Connection)
        app.connection = connection
        app.events = Mock(Events)

        VaadinSession vaadinSession = Mock() {
            hasLock() >> true
            getAttribute(App) >> app
            getAttribute(App.NAME) >> app
            getAttribute(Connection) >> connection
            getAttribute(Connection.NAME) >> connection
            getLocale() >> Locale.ENGLISH
        }
        VaadinSession.setCurrent(vaadinSession)

        ConnectorTracker vaadinConnectorTracker = Mock() {
            isWritingResponse() >> false
        }

        vaadinUi = Spy(AppUI)
        vaadinUi.app = app
        vaadinUi.messages = cont.getBean(Messages)
        vaadinUi.getConnectorTracker() >> vaadinConnectorTracker

        vaadinUi.applicationContext = cont.getApplicationContext()

        UI.setCurrent(vaadinUi)
    }

    void cleanup() {
        TestServiceProxy.clear()
    }

    static class TestApp extends DefaultApp {
        TestApp() {
            this.themeConstants = new ThemeConstants([:])
        }
    }
}