package nl.a11n.sailpoint;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;


import org.apache.commons.lang.StringUtils;

import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.QueryOptions;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;
import sailpoint.tools.JsonHelper;

import org.apache.log4j.Logger;

/**
 * @author Arie Timmerman
 */

@RequiredRight("IIQueryRunner")
@Path("iiquery")
@Consumes({ "application/json", "*/*" })
public class Main extends BasePluginResource {
	
	@XmlRootElement
	static class Search {
		public String hql;
		
		public Search(String hql) {
			this.hql = hql;
		}
	}

	private static final Logger logger = Logger.getLogger(Main.class);

	@Override
	public String getPluginName() {
		return "IIQuery";
	}

	@POST
	@Path("search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"application/json;response-pass-through=true"})	@AllowAll
	public Response search(Map search) throws GeneralException {
		
		logger.error(search);

		SailPointContext context = SailPointFactory.getCurrentContext();

		QueryOptions queryOptions = new QueryOptions();
		queryOptions.setResultLimit((Integer)search.get("limit"));
		queryOptions.setFirstRow((Integer)search.get("first"));

		Iterator iterator = context.search(
			(String)search.get("hql")
		, null ,queryOptions);

		List<String> list = new ArrayList<String>();
		
		while(iterator.hasNext()) {
			list.add(JsonHelper.toJson(iterator.next()));
		}
		
		return Response.ok("[" + StringUtils.join(list, ",") + "]").build();

	}
}
