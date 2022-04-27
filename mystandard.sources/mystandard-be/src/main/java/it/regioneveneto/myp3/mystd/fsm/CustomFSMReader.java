/**
 *     My Standard
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.myp3.mystd.fsm;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CustomFSMReader implements java.io.Serializable {

    /*
     * This field specifies the file name
     */
    private String _ConfigFileName;
    /*
     * This field specifies the file stream
     */
    private InputStream _ConfigFileStream;
    private File fXmlFile = null;
    private Document doc = null;

    /*
     * Section to mark the tags to be read from
     * the XML Configuration file
     */
    public static final String __STATE_TAG = "STATE";
    public static final String __ID_TAG = "id";
    public static final String __NEXT_STATE_TAG = "nextState";
    public static final String __ROLE_TAG = "role";
    public static final String __ENTE_NAZIONALE_TAG = "enteNazionale";
    public static final String __OPERATORE_LOCALE_PROPRIO_ENTE_TAG = "opeLocaleEnte";
    public static final String __ENTITA_STRUTTURATA_TAG = "entitaStrutturata";
    public static final String __SPECIALIZZAZIONE_TAG = "specializzazione";
    public static final String __FINAL_STATE_TAG = "finalState";
    public static final String __SPECIALIZZAZIONE_FINAL_STATE_TAG = "specializzazioneFinalState";


    /**
     *
     * @param configFile
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public CustomFSMReader(String configFile) throws ParserConfigurationException, SAXException, IOException {
        this._ConfigFileName = configFile;
        this.fXmlFile = new File(this._ConfigFileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        try(final InputStream stream = Files.newInputStream(Paths.get(
                this._ConfigFileName))) {
            doc = dBuilder.parse(stream);//Parsing inputstream from file
        } catch (IOException e) {//Get from classloader
            try(final InputStream input = CustomFSMReader.class.getResourceAsStream("/" + this._ConfigFileName)) {
                doc = dBuilder.parse(input);
            } catch (Exception ex) {
                throw ex;
            }
        }

        doc.getDocumentElement().normalize();
    }


    /*
     * Expected XML Format
     * <FSM>
     *  <STATE id="state">
     *      <MESSAGE id="message01" action="action01" nextState="next01">
     *      </MESSAGE>
     *      <MESSAGE id="message02" action="action02" nextState="next02">
     *      </MESSAGE>
     *  </STATE>
     * </FSM>
     */

    /**
     *
     * @return
     */
    public List<String> getStates() {
        List<String> _a = new ArrayList();
        NodeList nList = this.doc.getElementsByTagName(this.__STATE_TAG);

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                _a.add(((Element)nNode).getAttribute(__ID_TAG));
            }
        }
        return _a;
    }

    /**
     *
     * @param stateId
     * @return
     */
    public Node getStateNode(String stateId) {
        Node _a = null;
        NodeList nList = this.doc.getElementsByTagName(this.__STATE_TAG);
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE &&
                    ((Element)nNode).getAttribute(__ID_TAG).equals(stateId)) {
                _a = nNode;
            }
        }
        return _a;
    }

    /**
     *
     * @param StateId
     * @return
     */
    public List<JSONObject> getStateInfo(String StateId) {
        List<JSONObject> stateList = new ArrayList<>();
        Element element = (Element)getStateNode(StateId);
        if( element == null) return stateList;
        NodeList nList = element.getChildNodes();
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            JSONObject node = new JSONObject();
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                node.put(__ROLE_TAG, ((Element)nNode).getAttribute(this.__ROLE_TAG));//Ruoli che possono eseguire l'operazione
                node.put(__ID_TAG, ((Element)nNode).getAttribute(this.__ID_TAG));//Id operazione
                node.put(__ENTE_NAZIONALE_TAG, ((Element)nNode).getAttribute(this.__ENTE_NAZIONALE_TAG));//Se l'operazione si può eseguire su entità definita da ente nazionale o no (locale)
                node.put(__OPERATORE_LOCALE_PROPRIO_ENTE_TAG, ((Element)nNode).getAttribute(this.__OPERATORE_LOCALE_PROPRIO_ENTE_TAG));//Se l'operazione è possibile per operatore locale solo se l'entità è stata definita dal proprio ente
                node.put(__ENTITA_STRUTTURATA_TAG, ((Element)nNode).getAttribute(this.__ENTITA_STRUTTURATA_TAG));//Operazione possibile solo se entità è una sottoclasse di entità strutturata
                node.put(__SPECIALIZZAZIONE_TAG, ((Element)nNode).getAttribute(this.__SPECIALIZZAZIONE_TAG));//Operazione possibile solo se entità è una sottoclasse di entità strutturata
                node.put(__NEXT_STATE_TAG, ((Element)nNode).getAttribute(this.__NEXT_STATE_TAG));//prossimo stato dell'entità dopo l'esecuzione dell'operazione
                node.put(__FINAL_STATE_TAG, element.getAttribute(__FINAL_STATE_TAG));//Not child, father element. Indica se lo stato è finale oppure no (se no, è mostrabile in bacheca)
                node.put(__SPECIALIZZAZIONE_FINAL_STATE_TAG, element.getAttribute(__SPECIALIZZAZIONE_FINAL_STATE_TAG));//Not child, father element. Indica se lo stato è finale oppure no (se no, è mostrabile in bacheca)

                stateList.add(node);
            }
        }
        return stateList;
    }
}