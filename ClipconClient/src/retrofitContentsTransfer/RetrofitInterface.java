package retrofitContentsTransfer;

import java.util.Map;

import application.Main;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;

public interface RetrofitInterface {
	// Path: [Protocol]://[URL]/[Resource Path]
	public final String PROTOCOL = "http://";
	public final String CONTEXT_ROOT = "websocketServerModule";

	public static final String BASE_URL = PROTOCOL + Main.SERVER_URI_PART + CONTEXT_ROOT + "/";

	/** upload */
	@Multipart
	@Headers({ "User-Agent: pcProgram" })
	@POST("UploadServlet")
	Call<ResponseBody> uploadMultipartData(@Part("userName") RequestBody username, @Part("groupPK") RequestBody grouppk, @Part MultipartBody.Part file);

	/** download */
	@Streaming
	@Headers({ "User-Agent: pcProgram" })
	@GET("DownloadServlet")
	Call<ResponseBody> requestDataDownload2(@QueryMap Map<String, String> parameters);
}
