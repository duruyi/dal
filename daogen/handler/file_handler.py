
import subprocess
from subprocess import PIPE
import glob, os, shutil, json
from daogen.generator.java_gen import generator
from daogen.handler.base import RequestDispatcher

projects_dir = os.path.join(
				os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
				"projects")

class FileHandler(RequestDispatcher):

	def index(self):
		files = glob.iglob(os.path.join(projects_dir, "*.jar"))

		#pom = glob.iglob(os.path.join(projects_dir, "*.xml"))

		names = [os.path.basename(f) for f in files]

		#names.extend([os.path.basename(p) for p in pom])

		self.render("../templates/file.html", files=names)

	def generate(self):
		project_id = self.get_argument("project_id", default=None, strip=False)
		generator.generate(project_id)

		working_dir = os.path.join(projects_dir,project_id)

		p = subprocess.Popen("mvn package",
			cwd=working_dir,
			shell=True, 
			stdout=PIPE, 
			stderr=PIPE)
		output, error = p.communicate()
		result = {
			"output": output,
			"error": error
		}

		print result

		files = glob.iglob(os.path.join(
			os.path.join(working_dir,"target"),
			 "*.jar"))

		for f in files:
			if os.path.isfile(f):
				shutil.copy2(f, projects_dir)

		self.write(json.dumps(result, encoding="GB2312"))
		self.finish()